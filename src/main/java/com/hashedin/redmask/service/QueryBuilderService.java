package com.hashedin.redmask.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskingRule;
import com.hashedin.redmask.configurations.MaskingRuleFactory;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used build part of the query that would be combined to build the necessary masked
 * view.
 */
public class QueryBuilderService {

  private static final Logger log = LoggerFactory.getLogger(QueryBuilderService.class);

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String SELECT_QUERY = "SELECT * FROM ";
  private static final String DEFAULT_INPUT_TABLE_SCHEMA = "public";

  public void buildFunctionsAndQueryForView(
      MaskingRule rule,
      FileWriter writer,
      MaskConfiguration config,
      String url)
      throws IOException, RedmaskConfigException {

    Set<String> functionDefinitionSet = new LinkedHashSet<>();
    List<String> querySubstring = new ArrayList<>();
    ResultSetMetaData rs = null;
    TemplateConfiguration templateConfig = config.getTemplateConfig();
    // get all columns of given table.
    log.info("Getting column metadata from existing table.");
    String query = SELECT_QUERY + rule.getTable();

    try (Connection CONN = DriverManager.getConnection(url,
        config.getSuperUser(), config.getSuperUserPassword());
         Statement STATEMENT = CONN.createStatement()) {
      if (!isValidTable(CONN, rule.getTable())) {
        throw new RedmaskConfigException(String.format("{} was not found.", rule.getTable()));
      }
      rs = STATEMENT.executeQuery(query).getMetaData();
      MaskingRuleFactory columnRuleFactory = new MaskingRuleFactory();
      log.info("Storing masking function names required to create the intended view.");
      Map<String, MaskingRuleDef> colMaskRuleMap = new HashMap<>();

      // Create a map of column and their associated masking rule.
      createColumnMaskRuleMap(rule, CONN, columnRuleFactory, colMaskRuleMap);

      // TODO :Validate maskType and column type compatibility.
      // Dynamically build sub query part for create view.
      getColumnMaskSubQueries(rule, functionDefinitionSet, querySubstring, rs, templateConfig,
          colMaskRuleMap);

    } catch (SQLException exception) {
      throw new RedmaskRuntimeException(
          "SQL Exception occurred while fetching original table data", exception);
    }

    log.info("Appending Masking function definition to the temporary redmask-masking.sql file.");
    for (String functionDefinition : functionDefinitionSet) {
      //Appending each masking function to redmask-masking.sql file
      writer.append(functionDefinition);
    }

    // Create view
    log.info("Creating the query in order to create the intended view.");
    String queryString = String.join(",", querySubstring);
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(rule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT "
        + queryString + " FROM " + rule.getTable() + ";";

    writer.append("\n\n-- Create masked view.\n");
    writer.append(createViewQuery);

    writer.append("\n\n-- Revoking access from original table");
    writer.append("\nREVOKE ALL ON TABLE " + DEFAULT_INPUT_TABLE_SCHEMA + "."
        + rule.getTable() + " FROM " + config.getUser() + ";");

  }

  /**
   * This function add all the dynamically design sub-queries based on the columns dn the mask type
   * and adds it into the querySubstring list.
   *
   * @param rule                  The masking rule for a given table.
   * @param functionDefinitionSet A set to store the unique function definition to be created to
   *                              execute the necessary masked view.
   * @param querySubstring        A list to store sub-queries built dynamically for making the
   *                              view query.
   * @param rs                    The ResultSet object contain the metadata for column of the table
   *                              specified in the
   *                              masking rule.
   * @param templateConfig        Template Confguration in order to access the different templates
   *                              to create user specific funciton definition and sub-queries
   * @param colMaskRuleMap        Map of the column to be masked and their masking function
   * @throws SQLException
   */
  void getColumnMaskSubQueries(
      MaskingRule rule,
      Set<String> functionDefinitionSet,
      List<String> querySubstring,
      ResultSetMetaData rs,
      TemplateConfiguration templateConfig,
      Map<String, MaskingRuleDef> colMaskRuleMap)
      throws SQLException {
    for (int i = 1; i <= rs.getColumnCount(); i++) {
      String colName = rs.getColumnName(i);
      if (colMaskRuleMap.containsKey(colName)) {
        querySubstring.add(colMaskRuleMap.get(colName)
            .getSubQuery(templateConfig, rule.getTable()));
        colMaskRuleMap.get(colName).addFunctionDefinition(templateConfig, functionDefinitionSet);
      } else {
        querySubstring.add(rs.getColumnName(i));
      }
    }
  }

  /**
   * Creates a map of the column name and the associated masking rule.
   *
   * @param rule              The masking rule given for a table.
   * @param connection        Connection to the SQL database
   * @param columnRuleFactory Factory class in order to convert the column rule to a specific
   *                          rule class
   * @param colMaskRuleMap    Map containing the column name as key and the specific rule class as
   *                          value.
   */
  void createColumnMaskRuleMap(
      MaskingRule rule,
      Connection connection,
      MaskingRuleFactory columnRuleFactory,
      Map<String, MaskingRuleDef> colMaskRuleMap) {
    for (ColumnRule col : rule.getColumns()) {
      // Build MaskingRuleDef object.
      if (!isValidTableColumn(connection, rule.getTable(), col.getColumnName())) {

        throw new RedmaskConfigException(
            String.format("{} was not found in {} table.", col.getColumnName(), rule.getTable()));
      } else {
        MaskingRuleDef def = buildMaskingRuleDef(col);
        colMaskRuleMap.put(col.getColumnName(), columnRuleFactory.getColumnMaskingRule(def));
      }
    }
  }


  /**
   * It generates the SQL query to drops the schema if it already exists.
   *
   * @param schemaName The name of the schema to be dropped.
   * @return SQL query to drop the intended schema.
   */
  public String dropSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
        .append("-- Drop " + schemaName + "Schema if it exists.")
        .append(NEW_LINE);

    sb.append("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;")
        .append(NEW_LINE);
    return sb.toString();
  }

  /**
   * It generates the SQL query inorder to create a new schema.
   *
   * @param schemaName The name of the schema to be created.
   * @return The SQL query to create the intended schema.
   */
  public String createSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
        .append("-- Create " + schemaName + " schema.")
        .append(NEW_LINE);

    sb.append("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";")
        .append(NEW_LINE);
    return sb.toString();
  }

  private MaskingRuleDef buildMaskingRuleDef(ColumnRule colRule) {
    Map<String, String> maskParams = new ObjectMapper().
        convertValue(colRule.getMaskParams(),
            new TypeReference<Map<String, String>>() {
            });

    return new MaskingRuleDef(colRule.getColumnName(),
        colRule.getMaskType(), maskParams) {

      @Override
      public void addFunctionDefinition(
          TemplateConfiguration config,
          Set<String> funcSet) {
      }

      @Override
      public String getSubQuery(TemplateConfiguration config, String tableName) {
        return "";
      }

    };
  }

  /**
   * This function check whether a table exists in the connected Database.
   *
   * @param connection Connection Object to the intended Database.
   * @param tableName  The name of the table to be checked.
   * @return True, if the Table is present in the database else false.
   */
  private boolean isValidTable(Connection connection, String tableName) {
    try {
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet rs = metaData.getTables(null, null, tableName, null);
      if (rs.next()) {
        return true;
      }
      return false;
    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(
          "Error getting metadata from SQL Database for table " + tableName + ".", ex);
    }
  }

  /**
   * This function check whether a column exists in a particular table.
   *
   * @param connection Connection Object to the intended Database.
   * @param tableName  The name of the table that should contain the column.
   * @param columnName The name of the column to be checked.
   * @return True, if the column is present in the table,else false.
   */
  private boolean isValidTableColumn(Connection connection, String tableName, String columnName) {
    try {
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
      if (rs.next()) {
        return true;
      }
      return false;
    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(
          "Error getting metadata from SQL Database for table " + tableName + ".", ex);
    }
  }
}
