package com.hashedin.redmask.service;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingRule;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class QueryBuilderUtil {

  private static final String newLine = System.getProperty("line.separator");
  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

  public static void generateQueryToCreateMaskingFunctions(FileWriter writer,
      MaskConfiguration config)
          throws TemplateNotFoundException, MalformedTemplateNameException,
          ParseException, IOException, TemplateException {

    // This set will hold values of all masking rule provided in config file.
    Set<MaskType> maskingSet = new HashSet<>();
    for (MaskingRule maskingRule : config.getRules()) {
      for (ColumnRule col: maskingRule.getColumns()) {
        maskingSet.add(col.getRule());
      }
    }

    // Create functions for all the required masking rule.
    for(MaskType type: maskingSet) {
      switch(type) {
        case RANDOM_PHONE:
          MaskingFunctionQuery.randomPhone(config, writer);
          break;
        case STRING_MASKING:
          MaskingFunctionQuery.maskString(config, writer);
          break;
        case EMAIL_SHOW_FIRST_CHARACTERS:
          MaskingFunctionQuery.maskString(config, writer);
          break;
        case EMAIL_SHOW_DOMAIN: //Intentional fall-through
        case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN:
        case EMAIL_MASK_ALPHANUMERIC:
          MaskingFunctionQuery.maskEmail(config, writer);
          break;
        case RANDOM_INTEGER_FIXED_WIDTH:
          MaskingFunctionQuery.maskIntegerInRange(config, writer);
          MaskingFunctionQuery.maskIntegerFixedSize(config, writer);
          break;
        case RANDOM_INTEGER_WITHIN_RANGE:
          MaskingFunctionQuery.maskIntegerInRange(config, writer);
          break;
        case INTEGER_FIXED_VALUE:
          MaskingFunctionQuery.maskIntegerFixedValue(config, writer);
          break;
        case FLOAT_FIXED_VALUE:
          MaskingFunctionQuery.maskFloatFixedValue(config, writer);
          break;
        case NUMERIC_RANGE:
          MaskingFunctionQuery.maskIntegerRange(config, writer);
          MaskingFunctionQuery.maskNumericRange(config, writer);
          break;
        case INTEGER_RANGE:
          MaskingFunctionQuery.maskIntegerRange(config, writer);
          break;
        case BIGINT_RANGE:
          MaskingFunctionQuery.maskBigIntRange(config, writer);
          break;
        case MEAN_VALUE:
          MaskingFunctionQuery.maskMean(config, writer);
          break;
        case MODE_VALUE:
          MaskingFunctionQuery.maskMode(config, writer);
          break;
        case CREDIT_CARD_SHOW_FIRST: //Intentional fall-through
        case CREDIT_CARD_SHOW_LAST:
        case CREDIT_CARD_SHOW_FIRST_LAST:
          MaskingFunctionQuery.maskCard(config, writer);
          break;
        case DESTRUCTION:
          // Do something for destruction masking type.
          break;
        default:
          break;
      }
    }
  }
  
  public static void buildQueryForView(MaskingRule rule, FileWriter writer,
      MaskConfiguration config, String url)
      throws SQLException, IOException {

    // get all columns of given table.
    String query = "SELECT * FROM " + rule.getTable();

    // TODO: Use String builder here.
    String querySubstring = "";
    ResultSetMetaData rs = null;

    try(Connection conn = DriverManager.getConnection(url,
        config.getSuperUser(), config.getSuperUserPassword());
        Statement st = conn.createStatement()) {
      rs = st.executeQuery(query).getMetaData();
    }

    Map<String, MaskType> colMaskRuleMap = new HashMap<>();
    for (ColumnRule col : rule.getColumns()) {
      colMaskRuleMap.put(col.getName(), col.getRule());
    }

    // TODO: Add validation, if column to be masked does not exists.

    // Dynamically build sub query part for create view.
    for (int i = 1; i <= rs.getColumnCount(); i++) {
      String colName = rs.getColumnName(i);

      // TODO: Remove this if conditions and improve logic here.
      if (colMaskRuleMap.containsKey(colName)) {

        switch(colMaskRuleMap.get(colName)) {
          case RANDOM_PHONE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "." + "random_phone()"
                + " as " + colName;
            break;
          }
          case STRING_MASKING: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "anonymize(" + colName + ") as " + colName;
            break;
          }
          case EMAIL_SHOW_DOMAIN: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "emailmask(" + colName + ")" +" as " + colName;
            break;
          }
          case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "emailmask(" + colName + ",'firstndomain') as " + colName;
            break;
          }
          case EMAIL_SHOW_FIRST_CHARACTERS: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "emailmask(" + colName + ",'firstN') as " + colName;
            break;
          }
          case EMAIL_MASK_ALPHANUMERIC: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "emailmask(" + colName + ",'nonspecialcharacter') as " + colName;
            break;
          }
          case RANDOM_INTEGER_FIXED_WIDTH: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "generate(" + colName + ",5) as " + colName;
            break;
          }
          case RANDOM_INTEGER_WITHIN_RANGE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "random_int_between(" + colName + ",1,10) as " + colName;
            break;
          }
          case INTEGER_FIXED_VALUE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "replaceby(" + colName + ",5) as " + colName;
            break;
          }
          case FLOAT_FIXED_VALUE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "replaceby(" + colName + ",7.0) as " + colName;
            break;
          }
          case NUMERIC_RANGE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "range_numeric(" + colName + ") as " + colName;
            break;
          }
          case INTEGER_RANGE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "range_int4(" + colName + ") as " + colName;
            break;
          }
          case BIGINT_RANGE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "range_int8(" + colName + ") as " + colName;
            break;
          }
          case MEAN_VALUE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "substitute_mean('" + colName + "','"+ rule.getTable()+ "') as " + colName;
            break;
          }
          case MODE_VALUE: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "substitute_mode('" + colName + "','"+ rule.getTable()+ "') as " + colName;
            break;
          }
          case CREDIT_CARD_SHOW_FIRST: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "cardmask(" + colName + ",'first') as " + colName;
            break;
          }
          case CREDIT_CARD_SHOW_LAST: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "cardmask(" + colName + ") as " + colName;
            break;
          }
          case CREDIT_CARD_SHOW_FIRST_LAST: {
            querySubstring += ", " + MASKING_FUNCTION_SCHEMA + "."
                + "cardmask(" + colName + ",'firstnlast','',4,4) as " + colName;
            break;
          }
        case DESTRUCTION:
          // Do something.
          break;
        default:
          break;
        }
      } else if(querySubstring.isEmpty()) {
        querySubstring = rs.getColumnName(i);
      } else {
        querySubstring += ", " + rs.getColumnName(i);
      }
    }

    // Create view
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(rule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT " +
        querySubstring +  " FROM " + rule.getTable() + ";";

    writer.append("\n\n-- Create masked view.\n");
    writer.append(createViewQuery);
  }

  public static String dropSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(newLine)
      .append("-- Drop " + schemaName + "Schema if it exists.")
      .append(newLine);

    sb.append("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;")
      .append(newLine);
    return sb.toString();
  }

  public static String createSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(newLine)
      .append("-- Create " + schemaName + " schema.")
      .append(newLine);

    sb.append("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";")
      .append(newLine);
    return sb.toString();
  }

}
