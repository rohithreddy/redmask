package com.hashedin.redmask.common;

import java.io.File;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.config.ColumnRule;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.config.TemplateConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import com.hashedin.redmask.factory.DataBaseType;
import com.hashedin.redmask.factory.MaskingRuleFactory;

public class QueryBuilderUtil {

  private static final Logger log = LoggerFactory.getLogger(QueryBuilderUtil.class);
  private static final String SELECT_QUERY = "SELECT * FROM ";
  private static final String DEFAULT_INPUT_TABLE_SCHEMA = "public";

  private QueryBuilderUtil() {}

  public static void buildFunctionsAndQueryForView(
      MaskingRule rule,
      FileWriter writer,
      MaskConfiguration config,
      String url)
      throws RedmaskConfigException {
    
    Set<String> functionDefinitionSet = new LinkedHashSet<>();
    List<String> querySubstring = new ArrayList<>();
    ResultSetMetaData rs = null;
    TemplateConfiguration templateConfig = config.getTemplateConfig();
    String dbType = config.getDbType().toString().toLowerCase();
    
    if (dbType.equalsIgnoreCase(DataBaseType.POSTGRES.toString())) {
      try {
        Class.forName("com.amazon.redshift.jdbc.Driver");
      } catch (ClassNotFoundException ex) {
        throw new RedmaskRuntimeException(ex);
      }
    }
    
    try (Connection CONN = DriverManager.getConnection(url,
        config.getSuperUser(), config.getSuperUserPassword());
         Statement STATEMENT = CONN.createStatement()) {

      // Check if given table exists.
      if (!QueryBuilderUtil.isValidTable(CONN, rule.getTable())) {
        throw new RedmaskConfigException(String.format("Table {} was not found.", rule.getTable()));
      }

      String query = SELECT_QUERY + rule.getTable();
      rs = STATEMENT.executeQuery(query).getMetaData();
      MaskingRuleFactory maskRuleFactory = new MaskingRuleFactory();

      log.info("Storing masking function names required to create the intended masked view.");
      Map<String, MaskingRuleDef> colMaskRuleMap = new HashMap<>();

      // Create a map of column and their associated masking rule.
      QueryBuilderUtil.createColumnMaskRuleMap(rule, CONN, maskRuleFactory, colMaskRuleMap);

      // TODO :Validate maskType and column type compatibility.
      // Dynamically build sub query part for create view.
      // FIXME Pass enum value of dbtype instead of string.
      QueryBuilderUtil.getColumnMaskSubQueries(rule, functionDefinitionSet, 
          querySubstring, rs, templateConfig,
          colMaskRuleMap, dbType);

    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(
          "SQL Exception occurred while fetching original table data", ex);
    }

    /**
     * Inserting Masking function definition to the temporary redmask-masking.sql file.
     * Appending each masking function to redmask-masking.sql file
     */
    for (String functionDefinition : functionDefinitionSet) {
      try {
        writer.append(functionDefinition);
      } catch (IOException e) {
        throw new RedmaskRuntimeException(
            "Exception while appending masking function definition to file writer", e);
      }
    }

    // Create view query.
    log.info("Creating the query in order to create the intended masked view.");
    String queryString = String.join(",", querySubstring);
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(rule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT "
        + queryString + " FROM " + rule.getTable() + ";";

    try {
      writer.append("\n\n-- Create masked view.\n");
      writer.append(createViewQuery);

      writer.append("\n\n-- Revoking access from original table");
      writer.append("\nREVOKE ALL ON TABLE " + DEFAULT_INPUT_TABLE_SCHEMA + "."
          + rule.getTable() + " FROM " + config.getUser() + ";");
    } catch (IOException e) {
      throw new RedmaskRuntimeException(
          "Exception while appending masking function definition to file writer", e);
    }

  }
  
  public static MaskingRuleDef buildMaskingRuleDef(ColumnRule colRule) {
    Map<String, String> maskParams = new ObjectMapper().convertValue(
        colRule.getMaskParams(), new TypeReference<Map<String, String>>() { });

    return new MaskingRuleDef(colRule.getColumnName(),
        colRule.getMaskType(), maskParams) {

      @Override
      public void addFunctionDefinition(
          TemplateConfiguration config,
          Set<String> funcSet, String dbType) {
      }

      @Override
      public String getSubQuery(TemplateConfiguration config, String tableName) {
        return "";
      }

    };
  }

  /**
   * Creates a map of the column name and the associated masking rule.
   *
   * @param rule              The masking rule given for a table.
   * @param connection        Connection object to the SQL database
   * @param columnRuleFactory Factory class in order to convert the column rule to a specific
   *                          rule class
   * @param colMaskRuleMap    Map containing the column name as key and the specific rule class as
   *                          value.
   * @throws SQLException 
   */
  public static void createColumnMaskRuleMap(
      MaskingRule rule,
      Connection connection,
      MaskingRuleFactory columnRuleFactory,
      Map<String, MaskingRuleDef> colMaskRuleMap) throws SQLException {

    // For each column rule in a given table.
    for (ColumnRule col : rule.getColumns()) {
      if (!QueryBuilderUtil.isValidTableColumn(connection, rule.getTable(), col.getColumnName())) {
        throw new RedmaskConfigException(
            String.format("Column {} was not found in {} table.",
                col.getColumnName(), rule.getTable()));
      } else {
        // Build MaskingRuleDef object.
        MaskingRuleDef def = QueryBuilderUtil.buildMaskingRuleDef(col);
        colMaskRuleMap.put(col.getColumnName(), columnRuleFactory.getColumnMaskingRule(def));
      }
    }
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
   * @param templateConfig        Template Configuration in order to access the different templates
   *                              to create user specific function definition and sub-queries
   * @param colMaskRuleMap        Map of the column to be masked and their masking function
   * @throws SQLException
   */
  public static void getColumnMaskSubQueries(
      MaskingRule rule,
      Set<String> functionDefinitionSet,
      List<String> querySubstring,
      ResultSetMetaData rs,
      TemplateConfiguration templateConfig,
      Map<String, MaskingRuleDef> colMaskRuleMap,
      String dbType)
          throws SQLException {
    // For each column in a given table.
    for (int i = 1; i <= rs.getColumnCount(); i++) {
      String colName = rs.getColumnName(i);
      if (colMaskRuleMap.containsKey(colName)) { // Check if masking has to be done for this column.
        querySubstring.add(colMaskRuleMap.get(colName)
            .getSubQuery(templateConfig, rule.getTable()));
        colMaskRuleMap.get(colName).addFunctionDefinition(templateConfig, functionDefinitionSet,
            dbType);
      } else {
        querySubstring.add(rs.getColumnName(i));
      }
    }
  }

  /**
   * This function check whether a table exists in the connected Database.
   *
   * @param connection Connection Object to the intended Database.
   * @param tableName  The name of the table to be checked.
   * @return True, if the Table is present in the database else false.
   */
  public static boolean isValidTable(Connection connection, String tableName) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet rs = metaData.getTables(null, null, tableName, null);
    if (rs.next()) {
      rs.close();
      return true;
    }
    rs.close();
    return false;
  }

  /**
   * This function check whether a column exists in a particular table.
   *
   * @param connection Connection Object to the intended Database.
   * @param tableName  The name of the table that should contain the column.
   * @param columnName The name of the column to be checked.
   * @return True, if the column is present in the table else false.
   */
  public static boolean isValidTableColumn(Connection connection, String tableName,
      String columnName) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
    if (rs.next()) {
      rs.close();
      return true;
    }
    rs.close();
    return false;
  }

  public static File createMaskingSqlFile() {
    // create a temp .sql file
    File sqlFile = null;
    try {
      sqlFile = File.createTempFile("redmask-masking", ".sql");
      log.info("Created a temp file at location: {}", sqlFile.getAbsolutePath());
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          "Error while creating temporary file \'redmask-masking.sql\'", ex);
    }
    return sqlFile;
  }

}
