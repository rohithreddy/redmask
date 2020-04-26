package com.hashedin.redmask.service;

import com.hashedin.redmask.configurations.TemplateConfiguration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hashedin.redmask.configurations.MaskingConstants.MASK_BIGINT_RANGE_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_BIGINT_RANGE_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_BIGINT_RANGE_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_CARD_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_CARD_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_CARD_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_EMAIL_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_EMAIL_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_EMAIL_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_FLOAT_FIXED_VALUE_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_FLOAT_FIXED_VALUE_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_FLOAT_FIXED_VALUE_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_FIXED_SIZE_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_FIXED_SIZE_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_FIXED_SIZE_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_FIXED_VALUE_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_FIXED_VALUE_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_FIXED_VALUE_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_RANGE_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_RANGE_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_RANGE_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_WITHIN_RANGE_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_NUMBERS_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_NUMBERS_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_NUMBERS_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_NUMERIC_RANGE_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_NUMERIC_RANGE_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_NUMERIC_RANGE_FUNC;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_STRING_COMMENT;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_STRING_FILE;
import static com.hashedin.redmask.configurations.MaskingConstants.MASK_STRING_FUNC;

public class MaskingQueryUtil {

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";
  private static final String TEMPLATE_NAME = "create_function.txt";
  private static final String SCHEMA = "schema";
  private static final String MASKING_FUNCTION_NAME = "functionName";

  private MaskingQueryUtil() {
    // No Use.
  }

  public static final String maskString(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_STRING_FUNC);
    return MASK_STRING_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_STRING_FILE);
  }

  public static final String maskEmail(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_EMAIL_FUNC);
    return MASK_EMAIL_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_EMAIL_FILE);
  }

  public static final String maskIntegerFixedSize(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_FIXED_SIZE_FUNC);
    return MASK_INTEGER_FIXED_SIZE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_INTEGER_FIXED_SIZE_FILE);
  }

  public static final String maskIntegerInRange(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME,
        MASK_INTEGER_WITHIN_RANGE_FUNC);
    return MASK_INTEGER_WITHIN_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_INTEGER_WITHIN_RANGE_FILE);
  }

  public static final String maskIntegerFixedValue(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_FIXED_VALUE_FUNC);
    return MASK_INTEGER_FIXED_VALUE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_INTEGER_FIXED_VALUE_FILE);

  }

  public static final String maskFloatFixedValue(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_FLOAT_FIXED_VALUE_FUNC);
    return MASK_FLOAT_FIXED_VALUE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_FLOAT_FIXED_VALUE_FILE);
  }

  public static final String maskNumericRange(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMERIC_RANGE_FUNC);
    return MASK_NUMERIC_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_NUMERIC_RANGE_FILE);
  }

  public static final String maskIntegerRange(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_INTEGER_RANGE_FUNC);
    return MASK_INTEGER_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_INTEGER_RANGE_FILE);
  }

  public static final String maskBigIntRange(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_BIGINT_RANGE_FUNC);
    return MASK_BIGINT_RANGE_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_BIGINT_RANGE_FILE);
  }

  public static final String maskNumbers(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_NUMBERS_FUNC);
    return MASK_NUMBERS_COMMENT + createFuncString
        + readFunctionQueryFromSqlFile(MASK_NUMBERS_FILE);
  }

  public static final String maskCard(TemplateConfiguration config)
      throws IOException, TemplateException {
    String createFuncString = processTemplate(config, TEMPLATE_NAME, MASK_CARD_FUNC);
    return MASK_CARD_COMMENT + createFuncString + readFunctionQueryFromSqlFile(MASK_CARD_FILE);

  }

  private static String readFunctionQueryFromSqlFile(String filePath) throws IOException {
    // Creating a reader object
    FileInputStream sqlFunctionFile = new FileInputStream(filePath);
    return IOUtils.toString(sqlFunctionFile, StandardCharsets.UTF_8);
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


  //TO generate sub queries with variable parameter length
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
