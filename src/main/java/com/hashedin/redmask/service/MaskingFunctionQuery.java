package com.hashedin.redmask.service;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.hashedin.redmask.configurations.MaskConfiguration;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import static com.hashedin.redmask.configurations.MaskingConstants.*;

public class MaskingFunctionQuery {

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";
  private static final String TEMPLATE_NAME = "create_function.txt";


  public static final String randomPhone(MaskConfiguration config)
      throws TemplateNotFoundException, MalformedTemplateNameException,
      ParseException, IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, "random_phone");
    StringBuilder sb = new StringBuilder();
    sb.append(createFuncString);

    String subQuery = "(\n" +
        "  phone_prefix TEXT DEFAULT '0'\n" +
        ")\n" +
        "RETURNS TEXT AS $$\n" +
        "BEGIN\n" +
        "  RETURN (SELECT  phone_prefix\n" +
        "          || CAST(redmask.random_int_between(100000000,999999999) AS TEXT)\n" +
        "          AS \"phone\");\n" +
        "END\n" +
        "$$ LANGUAGE plpgsql;";
    sb.append(subQuery);

    // Create random phone number generation function.
    String comment = "\n\n-- Postgres function to generate ranadom phone number data.\n";
    return comment+sb.toString();
  }

  public static final String maskString(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_STRING_FUNC);
    return MASK_STRING_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_STRING_FILE);
  }

  //TODO change all function to the new pattern when the design os approved
  public static final String maskEmail(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_EMAIL_FUNC);
    return MASK_EMAIL_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_EMAIL_FILE);
  }

  public static final String maskIntegerFixedSize(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_FIXED_SIZE_FUNC);
    return MASK_INTEGER_FIXED_SIZE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_INTEGER_FIXED_SIZE_FILE);
  }

  public static final String maskIntegerInRange(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_WITHIN_RANGE_FUNC);
    return MASK_INTEGER_WITHIN_RANGE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_INTEGER_WITHIN_RANGE_FILE);
  }

  public static final String maskIntegerFixedValue(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_FIXED_VALUE_FUNC);
    return MASK_INTEGER_FIXED_VALUE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_INTEGER_FIXED_VALUE_FILE);

  }

  public static final String maskFloatFixedValue(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_FLOAT_FIXED_VALUE_FUNC);
    return MASK_FLOAT_FIXED_VALUE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_FLOAT_FIXED_VALUE_FILE);
  }

  public static final String maskNumericRange(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMERIC_RANGE_FUNC);
    return MASK_NUMERIC_RANGE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_NUMERIC_RANGE_FILE);
  }

  public static final String maskIntegerRange(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_RANGE_FUNC);
    return MASK_INTEGER_RANGE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_INTEGER_RANGE_FILE);
  }

  public static final String maskBigIntRange(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_BIGINT_RANGE_FUNC);
    return MASK_BIGINT_RANGE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_BIGINT_RANGE_FILE);
  }

  public static final String maskMean(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_MEAN_FUNC);
    return MASK_MEAN_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_MEAN_FILE);
  }

  public static final String maskMode(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_MODE_FUNC);
    return MASK_MODE_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_MODE_FILE);
  }

  public static final String maskNumbers(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMBERS_FUNC);
    return MASK_NUMBERS_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_NUMBERS_FILE);
  }

  public static final String maskCard(MaskConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_CARD_FUNC);
    return MASK_CARD_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_CARD_FILE);

  }

  private static String readFunctionQueryFromSqlFile(String filePath) throws IOException {

    // Creating a reader object
    FileInputStream sqlFunctionFile = new FileInputStream(filePath);
    return IOUtils.toString(sqlFunctionFile, StandardCharsets.UTF_8);

  }

  private static String processTemplate(MaskConfiguration config, String templateName, String functionName)
      throws TemplateNotFoundException, MalformedTemplateNameException,
      ParseException, IOException, TemplateException {
    Map<String, String> input = new HashMap<String, String>();
    input.put("schema", MASKING_FUNCTION_SCHEMA);
    input.put("functionName", functionName);
    Template temp = config.getTemplateConfig().getConfig().getTemplate(templateName);
    StringWriter stringWriter = new StringWriter();
    temp.process(input, stringWriter);
    String createFunString = stringWriter.toString();
    stringWriter.close();
    return createFunString;
  }
}
