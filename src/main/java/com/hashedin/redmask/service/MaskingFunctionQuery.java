package com.hashedin.redmask.service;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

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

import static com.hashedin.redmask.configurations.Constants.*;

public class  MaskingFunctionQuery {

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";
  private static final String TEMPLATE_NAME = "create_function.txt";

  public static final String randomIntegerBetween(MaskConfiguration config, FileWriter writer) 
      throws TemplateNotFoundException, MalformedTemplateNameException, 
      ParseException, IOException, TemplateException {

    String createFuncString = processTemplate(config, TEMPLATE_NAME, "random_int_between");
    StringBuilder sb = new StringBuilder();
    sb.append(createFuncString);
    String subQuery = "(int_start integer,\n" + 
        "  int_stop integer\n" + 
        ")\n" + 
        "RETURNS integer AS $$\n" + 
        "BEGIN\n" + 
        "    RETURN (SELECT CAST ( random()*(int_stop-int_start)+int_start AS integer ));\n" + 
        "END\n" + 
        "$$ LANGUAGE plpgsql;";
    sb.append(subQuery);

    // Create random phone number generation function.
    writer.append("\n\n-- Postgres function to generate random between given two integer.\n");
    writer.append(sb.toString());

    return sb.toString();
  }

  public static final void randomPhone(MaskConfiguration config, FileWriter writer)
      throws TemplateNotFoundException, MalformedTemplateNameException,
      ParseException, IOException, TemplateException {
    randomIntegerBetween(config, writer);
    String templateName = "create_function.txt";

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
    writer.append("\n\n-- Postgres function to generate ranadom phone number data.\n");
    writer.append(sb.toString());
  }

  public static final void maskString(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_STRING_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_STRING_FILE, writer, MASK_STRING_COMMENT);
  }

  public static final void maskEmail(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_EMAIL_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_EMAIL_FILE, writer, MASK_EMAIL_COMMENT);
  }

  public static final void maskIntegerFixedSize(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_FIXED_SIZE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_INTEGER_FIXED_SIZE_FILE, writer, MASK_INTEGER_FIXED_SIZE_COMMENT);
  }

  public static final void maskIntegerInRange(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_WITHIN_RANGE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_INTEGER_WITHIN_RANGE_FILE, writer, MASK_INTEGER_WITHIN_RANGE_COMMENT);
  }

  public static final void maskIntegerFixedValue(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_FIXED_VALUE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_INTEGER_FIXED_VALUE_FILE, writer, MASK_INTEGER_FIXED_VALUE_COMMENT);
  }

  public static final void maskFloatFixedValue(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_FLOAT_FIXED_VALUE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_FLOAT_FIXED_VALUE_FILE, writer, MASK_FLOAT_FIXED_VALUE_COMMENT);
  }

  public static final void maskNumericRange(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMERIC_RANGE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_NUMERIC_RANGE_FILE, writer, MASK_NUMERIC_RANGE_COMMENT);
  }

  public static final void maskIntegerRange(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_RANGE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_INTEGER_RANGE_FILE, writer, MASK_INTEGER_RANGE_COMMENT);
  }

  public static final void maskBigIntRange(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_BIGINT_RANGE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_BIGINT_RANGE_FILE, writer, MASK_BIGINT_RANGE_COMMENT);
  }

  public static final void maskMean(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_MEAN_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_MEAN_FILE, writer, MASK_MEAN_COMMENT);
  }

  public static final void maskMode(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_MODE_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_MODE_FILE, writer, MASK_MODE_COMMENT);
  }

  public static final void maskNumbers(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMBERS_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_NUMBERS_FILE, writer, MASK_NUMBERS_COMMENT);
  }

  public static final void maskCard(MaskConfiguration config, FileWriter writer)
      throws IOException, TemplateException  {
    maskNumbers(config, writer);
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_CARD_FUNC_NAME);
    writer.append(createFuncString);
    readFunctionQueryFromSqlFile(MASK_CARD_FILE, writer, MASK_CARD_COMMENT);

  }

  private static void readFunctionQueryFromSqlFile(String filePath,
      FileWriter writer, String comment) throws IOException {

    // Creating a reader object
    FileInputStream sqlFunctionFile = new FileInputStream(filePath);

    // Append function query.
    writer.append(comment);
    writer.append(IOUtils.toString(sqlFunctionFile, StandardCharsets.UTF_8));
    sqlFunctionFile.close();
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
