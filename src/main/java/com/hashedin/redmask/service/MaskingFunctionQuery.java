package com.hashedin.redmask.service;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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

  public static final void maskString(MaskConfiguration config, FileWriter writer) 
      throws IOException {

    // Creating a reader object
    FileInputStream sqlFunctionFile = new FileInputStream("src/main/resources/strings/AnonymizePartial.sql");

    // Create string anonymize function.
    writer.append("\n\n-- Postgres function to anonymize the string field.\n");
    writer.append(IOUtils.toString(sqlFunctionFile, "UTF-8"));
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
