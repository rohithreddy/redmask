package com.hashedin.redmask.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskingRule;
import com.hashedin.redmask.configurations.MaskingRuleFactory;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.exception.ColumnNotFoundException;
import com.hashedin.redmask.exception.InvalidParameterValueException;
import com.hashedin.redmask.exception.TableNotFoundException;
import com.hashedin.redmask.exception.UnknownParameterException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

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

public class QueryBuilderService {

  private static final Logger log = LogManager.getLogger(QueryBuilderService.class);

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String SELECT_QUERY = "SELECT * FROM ";

  public void buildFunctionsAndQueryForView(
      MaskingRule rule,
      FileWriter writer,
      MaskConfiguration config,
      String url)
      throws IOException, InvalidParameterValueException, UnknownParameterException,
      TableNotFoundException, ColumnNotFoundException {

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
        throw new TableNotFoundException(rule.getTable());
      }
      rs = STATEMENT.executeQuery(query).getMetaData();
      MaskingRuleFactory columnRuleFactory = new MaskingRuleFactory();
      log.info("Storing masking function names required to create the intended view.");
      Map<String, MaskingRuleDef> colMaskRuleMap = new HashMap<>();
      for (ColumnRule col : rule.getColumns()) {
        // Build MaskingRuleDef object.
        if (!isValidTableColumn(CONN, rule.getTable(), col.getColumnName())) {
          throw new ColumnNotFoundException(col.getColumnName(), rule.getTable());
        } else {
          MaskingRuleDef def = buildMaskingRuleDef(col);
          colMaskRuleMap.put(col.getColumnName(), columnRuleFactory.getColumnMaskingRule(def));
        }
      }
      //TODO Validate maskType and column type compatibility.
      // Dynamically build sub query part for create view.
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
    } catch (SQLException exception) {
      log.error("SQL Exception occurred while fetching original table data", exception);
    }


    log.info("Appending Masking function definition to the temporary redmask-masking.sql file.");
    for (String functionDefinition : functionDefinitionSet) {
      //Appending each masking function to redmask-masking.sql file
      writer.append(functionDefinition);
    }

    // Create view
    log.info("creating the query in order to create the intended view");
    String queryString = String.join(",", querySubstring);
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(rule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT "
        + queryString + " FROM " + rule.getTable() + ";";

    writer.append("\n\n-- Create masked view.\n");
    writer.append(createViewQuery);

  }

  public String dropSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
        .append("-- Drop " + schemaName + "Schema if it exists.")
        .append(NEW_LINE);

    sb.append("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;")
        .append(NEW_LINE);
    return sb.toString();
  }

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
        return Strings.EMPTY;
      }

    };
  }

  private boolean isValidTable(Connection connection, String tableName) {
    try {
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet rs = metaData.getTables(null, null, tableName, null);
      if (rs.next()) {
        return true;
      }
      return false;
    } catch (SQLException ex) {
      log.error("Error getting metadata from SQL Database");
    }
    return false;
  }

  private boolean isValidTableColumn(Connection connection, String tableName, String columnName) {
    try {
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
      if (rs.next()) {
        return true;
      }
      return false;
    } catch (SQLException ex) {
      log.error("Error getting metadata from SQL Database");
    }
    return false;
  }
}
