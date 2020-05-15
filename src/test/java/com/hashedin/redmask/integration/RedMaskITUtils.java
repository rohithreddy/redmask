package com.hashedin.redmask.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.config.ColumnRule;
import com.hashedin.redmask.config.MaskType;

import java.io.IOException;


public class RedMaskITUtils {
  protected RedMaskITUtils(){}

  public static ColumnRule getMaskRuleString(String columnName) throws JsonProcessingException {
    String json = "{ \"pattern\":\"*\", \"show_first\":1, \"show_last\":1}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName, MaskType.STRING_MASKING, maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleEmailDomain() throws JsonProcessingException {
    String json = "{}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("email",
        MaskType.EMAIL_SHOW_DOMAIN,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleEmailFirstCharDomain(String columnName)
      throws JsonProcessingException {
    String json = "{}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.EMAIL_SHOW_FIRST_CHARACTER_DOMAIN,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleEmailFirstNChar(String columnName)
      throws JsonProcessingException {
    String json = "{ \"show_first\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.EMAIL_SHOW_FIRST_CHARACTERS,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleEmailAlphaNum(String columnName)
      throws JsonProcessingException {
    String json = "{}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.EMAIL_MASK_ALPHANUMERIC,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleIntegerFixedSize(String columnName)
      throws JsonProcessingException {
    String json = "{ \"size\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.RANDOM_INTEGER_FIXED_WIDTH,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleIntegerWithinRange(String columnName) throws IOException {

    String json = "{ \"min\":1, \"max\":10 }";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.RANDOM_INTEGER_WITHIN_RANGE,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleIntegerFixedValue(String columnName)
      throws JsonProcessingException {
    String json = "{ \"value\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.INTEGER_FIXED_VALUE,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleFloatFixedValue(String columnName)
      throws JsonProcessingException {
    String json = "{ \"value\":3.5}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.FLOAT_FIXED_VALUE,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleNumericRange(String columnName)
      throws JsonProcessingException {
    String json = "{ \"step\":100}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName, MaskType.NUMERIC_RANGE, maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleIntegerRange(String columnName)
      throws JsonProcessingException {
    String json = "{ \"step\":10}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName, MaskType.INTEGER_RANGE, maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleBigIntRange(String columnName)
      throws JsonProcessingException {
    String json = "{ \"step\":1000}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName, MaskType.BIGINT_RANGE, maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleCardFirst(String columnName) throws JsonProcessingException {
    String json = "{ \"separator\":\"-\", \"show_first\":5, \"show_last\":0}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.CREDIT_CARD_SHOW_FIRST,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleCardLast(String columnName) throws JsonProcessingException {
    String json = "{ \"separator\":\"-\", \"show_first\":0, \"show_last\":5}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.CREDIT_CARD_SHOW_LAST,
        maskParams);
    return columnRule;
  }

  public static ColumnRule getMaskRuleCardFirstLast(String columnName)
      throws JsonProcessingException {
    String json = "{ \"separator\":\"-\", \"show_first\":3, \"show_last\":5}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule(columnName,
        MaskType.CREDIT_CARD_SHOW_FIRST_LAST,
        maskParams);
    return columnRule;
  }

}

