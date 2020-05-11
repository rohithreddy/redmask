package com.hashedin.redmask.common;

import com.hashedin.redmask.config.TemplateConfiguration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;

import static com.hashedin.redmask.config.MaskingConstants.MASK_BIGINT_RANGE_COMMENT; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_BIGINT_RANGE_FILE;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_BIGINT_RANGE_FUNC;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_CARD_COMMENT; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_CARD_FILE;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_CARD_FUNC;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_EMAIL_COMMENT;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_EMAIL_FILE; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_EMAIL_FUNC; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_FLOAT_FIXED_VALUE_COMMENT;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_FLOAT_FIXED_VALUE_FILE; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_FLOAT_FIXED_VALUE_FUNC; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_FIXED_SIZE_COMMENT; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_FIXED_SIZE_FILE;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_FIXED_SIZE_FUNC;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_FIXED_VALUE_COMMENT;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_FIXED_VALUE_FILE; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_FIXED_VALUE_FUNC; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_RANGE_COMMENT;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_RANGE_FILE; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_RANGE_FUNC; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_WITHIN_RANGE_COMMENT; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FILE;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_NUMBERS_COMMENT;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_NUMBERS_FILE; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_NUMBERS_FUNC; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_NUMERIC_RANGE_COMMENT;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_NUMERIC_RANGE_FILE; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_NUMERIC_RANGE_FUNC; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_STRING_COMMENT; 
import static com.hashedin.redmask.config.MaskingConstants.MASK_STRING_FILE;  
import static com.hashedin.redmask.config.MaskingConstants.MASK_STRING_FUNC;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contain function that will add the masking function to the created for the inputted
 * masking rules.
 */
public class MaskingQueryUtil {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String MASKING_FUNCTION_SCHEMA = "redmask";
  private static final String TEMPLATE_NAME = "create_function.txt";
  private static final String SCHEMA = "schema";
  private static final String MASKING_FUNCTION_NAME = "functionName";

  private MaskingQueryUtil() {
    // No Use.
  }

  /**
   * It generates the SQL query to drops the schema if it already exists.
   *
   * @param schemaName The name of the schema to be dropped.
   * @return SQL query to drop the intended schema.
   */
  public static String dropSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
      .append("-- Drop " + schemaName + "Schema if it exists.")
      .append(NEW_LINE);

    sb.append("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;")
      .append(NEW_LINE);
    return sb.toString();
  }
  
  /**
   * It generates the SQL query in order to create a new schema.
   *
   * @param schemaName The name of the schema to be created.
   * @return The SQL query to create the intended schema.
   */
  public static String createSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
        .append("-- Create " + schemaName + " schema.")
        .append(NEW_LINE);

    sb.append("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";")
        .append(NEW_LINE);
    return sb.toString();
  }

  public static final String maskString(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_STRING_FUNC);
    return MASK_STRING_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_STRING_FILE);
  }

  public static final String maskEmail(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_EMAIL_FUNC);
    return MASK_EMAIL_COMMENT + createFuncString + readFunctionQueryFromSqlFile(
        dbType + MASK_EMAIL_FILE);
  }

  public static final String maskPaymentCard(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_CARD_FUNC);
    return MASK_CARD_COMMENT + createFuncString + readFunctionQueryFromSqlFile(
        dbType + MASK_CARD_FILE);
  }

  public static final String maskIntegerFixedSize(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_FIXED_SIZE_FUNC);
    return MASK_INTEGER_FIXED_SIZE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_INTEGER_FIXED_SIZE_FILE);
  }

  public static final String maskIntegerInRange(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME,
        MASK_INTEGER_WITHIN_RANGE_FUNC);
    return MASK_INTEGER_WITHIN_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_INTEGER_WITHIN_RANGE_FILE);
  }

  public static final String maskIntegerFixedValue(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, 
        MASK_INTEGER_FIXED_VALUE_FUNC);
    return MASK_INTEGER_FIXED_VALUE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_INTEGER_FIXED_VALUE_FILE);

  }

  public static final String maskFloatFixedValue(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_FLOAT_FIXED_VALUE_FUNC);
    return MASK_FLOAT_FIXED_VALUE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_FLOAT_FIXED_VALUE_FILE);
  }

  public static final String maskNumericRange(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMERIC_RANGE_FUNC);
    return MASK_NUMERIC_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_NUMERIC_RANGE_FILE);
  }

  public static final String maskIntegerRange(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_RANGE_FUNC);
    return MASK_INTEGER_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_INTEGER_RANGE_FILE);
  }

  public static final String maskBigIntRange(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_BIGINT_RANGE_FUNC);
    return MASK_BIGINT_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_BIGINT_RANGE_FILE);
  }

  public static final String maskNumbers(TemplateConfiguration config, String dbType)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMBERS_FUNC);
    return MASK_NUMBERS_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(dbType + MASK_NUMBERS_FILE);
  }

  private static String readFunctionQueryFromSqlFile(String filePath) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(filePath);
    return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
  }

  //To generate schema specific function definition
  private static String processTemplate(TemplateConfiguration config,
                                        String templateName,
                                        String functionName)
      throws IOException, TemplateException {
    Map<String, String> input = new HashMap<String, String>();
    input.put(SCHEMA, MASKING_FUNCTION_SCHEMA);
    input.put(MASKING_FUNCTION_NAME, functionName);
    Template temp = config.getConfig().getTemplate(templateName);
    StringWriter stringWriter = new StringWriter();
    temp.process(input, stringWriter);
    String createFunString = stringWriter.toString();
    stringWriter.close();
    return createFunString;
  }

  /**
   * TO generate sub queries with variable parameter length
   */
  public static String processQueryTemplate(TemplateConfiguration config,
                                            String functionName,
                                            List<String> parameters)
      throws IOException, TemplateException {
    Map<String, Object> input = new HashMap<String, Object>();
    input.put(SCHEMA, MASKING_FUNCTION_SCHEMA);
    input.put(MASKING_FUNCTION_NAME, functionName);
    input.put("parameters", parameters);
    Template temp = config.getConfig().getTemplate("view_function_query.txt");
    StringWriter stringWriter = new StringWriter();
    temp.process(input, stringWriter);
    String createFunString = stringWriter.toString();
    stringWriter.close();
    return createFunString;
  }
}
