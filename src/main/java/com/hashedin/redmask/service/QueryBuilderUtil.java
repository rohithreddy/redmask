package com.hashedin.redmask.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hashedin.redmask.configurations.*;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class QueryBuilderUtil {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String SELECT_QUERY = "SELECT * FROM ";
  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

  public static void buildFunctionsAndQueryForView(MaskingRule rule, FileWriter writer,
      MaskConfiguration config, String url)
      throws SQLException, IOException, InstantiationException, IllegalAccessException, TemplateException {

    Set<String> functionDefinitionSet = new LinkedHashSet<>();
    List<String> querySubstring = new ArrayList<>();
    ResultSetMetaData rs = null;
    
    // get all columns of given table.
    String query = SELECT_QUERY + rule.getTable();

    try(Connection conn = DriverManager.getConnection(url,
        config.getSuperUser(), config.getSuperUserPassword());
        Statement st = conn.createStatement()) {
      rs = st.executeQuery(query).getMetaData();
    }
    
    MaskingRuleFactory columnRuleFactory = new MaskingRuleFactory();

    Map<String, ColumnRule> colMaskRuleMap = new HashMap<>();
    for (ColumnRule col : rule.getColumns()) {
      colMaskRuleMap.put(col.getName(), columnRuleFactory.getColumnMaskingRule(col));
    }

    // TODO: Add validation, if column to be masked does not exists.

    // Dynamically build sub query part for create view.
    for (int i = 1; i <= rs.getColumnCount(); i++) {
      String colName = rs.getColumnName(i);
      if (colMaskRuleMap.containsKey(colName)) {
          querySubstring.add(colMaskRuleMap.get(colName).getSubQuery(config, rule.getTable()));
          colMaskRuleMap.get(colName).addFunctionDefinition(config, functionDefinitionSet);
      } else {
        querySubstring.add(rs.getColumnName(i));
      }
    }

    for (String functionDefinition: functionDefinitionSet) {
      writer.append(functionDefinition);
    }

    // Create view
    String queryString = String.join(",", querySubstring);
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(rule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT " +
        queryString +  " FROM " + rule.getTable() + ";";

    writer.append("\n\n-- Create masked view.\n");
    writer.append(createViewQuery);
  }

  public static String dropSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
      .append("-- Drop " + schemaName + "Schema if it exists.")
      .append(NEW_LINE);

    sb.append("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;")
      .append(NEW_LINE);
    return sb.toString();
  }

  public static String createSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
      .append("-- Create " + schemaName + " schema.")
      .append(NEW_LINE);

    sb.append("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";")
      .append(NEW_LINE);
    return sb.toString();
  }

  public static String processQueryTemplate(MaskConfiguration config, String functionName, List<String> parameters)
      throws IOException, TemplateException {
    Map<String, Object> input = new HashMap<String, Object>();
    input.put("schema", MASKING_FUNCTION_SCHEMA);
    input.put("functionName", functionName);
    input.put("parameters",parameters);
    Template temp = config.getTemplateConfig().getConfig().getTemplate("view_function_query.txt");
    StringWriter stringWriter = new StringWriter();
    temp.process(input, stringWriter);
    String createFunString = stringWriter.toString();
    stringWriter.close();
    return createFunString;
  }

}
