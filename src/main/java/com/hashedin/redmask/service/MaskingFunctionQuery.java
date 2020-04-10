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

public class MaskingFunctionQuery {

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

  public static final String randomIntegerBetween(MaskConfiguration config, FileWriter writer) 
      throws TemplateNotFoundException, MalformedTemplateNameException, 
      ParseException, IOException, TemplateException {

    String templateName = "create_function.txt";

    String createFunString = processTemplate(config, templateName, "random_int_between");
    StringBuilder sb = new StringBuilder();
    sb.append(createFunString);
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
    writer.append("\n\n-- Postgres function to generate ranadom between given two integer.\n");
    writer.append(sb.toString());

    return sb.toString();
  }

  public static final void randomPhone(MaskConfiguration config, FileWriter writer)
      throws TemplateNotFoundException, MalformedTemplateNameException,
      ParseException, IOException, TemplateException {
    randomIntegerBetween(config, writer);
    String templateName = "create_function.txt";

    String createFunString = processTemplate(config, templateName, "random_phone");
    StringBuilder sb = new StringBuilder();
    sb.append(createFunString);

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

  public static final void maskString(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/strings/AnonymizePartial.sql";
    String comment = "\n\n-- Postgres function to anonymize the string field.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskEmail(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/SpecializedFunctions/Email.sql";
    String comment = "\n\n-- Postgres function to mask email type.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskIntegerFixedSize(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/Generate.sql";
    String comment = "\n\n-- Postgres function to generate random number of fixed length.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskIntegerInRange(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/RandomInt.sql";
    String comment = "\n\n-- Postgres function to generate a random number between a given range.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskIntegerFixedValue(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/ReplaceByInteger.sql";
    String comment = "\n\n-- Postgres function to anonymize the integer field with given value.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskFloatFixedValue(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/ReplaceByFloat.sql";
    String comment = "\n\n-- Postgres function to anonymize the float field with given value.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskNumericRange(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/RangeNumeric.sql";
    String comment = "\n\n-- Postgres function to convert numeric type to a range.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskIntegerRange(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/RangeInt4.sql";
    String comment = "\n\n-- Postgres function to convert to integer range.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskBigIntRange(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/RangeInt8.sql";
    String comment = "\n\n-- Postgres function to convert to big integer range.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskMean(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/SubstituteMean.sql";
    String comment = "\n\n-- Postgres function to anonymize the integer field by column mean.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskMode(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/Integer_float/SubstituteMode.sql";
    String comment = "\n\n-- Postgres function to anonymize the integer field by column mode.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskNumbers(FileWriter writer)
      throws IOException {
    String filePath = "src/main/resources/strings/AnonymizeNumber.sql";
    String comment = "\n\n-- Postgres function to anonymize number in a string.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);
  }

  public static final void maskcard(FileWriter writer)
      throws IOException {
    maskNumbers(writer);
    String filePath = "src/main/resources/SpecializedFunctions/CardMask.sql";
    String comment = "\n\n-- Postgres function to anonymize card details.\n";
    readFunctionQueryFromSqlFile(filePath, writer, comment);

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
