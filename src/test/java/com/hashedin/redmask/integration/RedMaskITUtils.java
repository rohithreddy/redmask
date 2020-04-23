package com.hashedin.redmask.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingRule;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RedMaskITUtils {

  private static final String TABLE_NAME = "customer"; 

  public static List<MaskingRule> createMaskRule(ColumnRule columnRule) {
    MaskingRule rule = new MaskingRule();
    rule.setTable(TABLE_NAME);
    rule.setColumns(Collections.singletonList(columnRule));
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    return maskingRuleList;
  }

  public static List<MaskingRule> getMaskRuleString() throws JsonProcessingException {
    String json = "{ \"separator\":\"*\", \"prefix\":1, \"suffix\":1,}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("name", MaskType.STRING_MASKING, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleEmailDomain() throws JsonProcessingException {
    String json = "{ \"val\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("email", MaskType.EMAIL_SHOW_DOMAIN, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleEmailFirstCharDomain() throws JsonProcessingException {
    String json = "{ \"val\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("email", MaskType.EMAIL_SHOW_FIRST_CHARACTER_DOMAIN, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleEmailFirstNChar() throws JsonProcessingException {
    String json = "{ \"val\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("email", MaskType.EMAIL_SHOW_FIRST_CHARACTERS, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleEmailAlphaNum() throws JsonProcessingException {
    String json = "{ \"val\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("email", MaskType.EMAIL_MASK_ALPHANUMERIC, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleIntegerFixedSize() throws JsonProcessingException {
    String json = "{ \"size\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.RANDOM_INTEGER_FIXED_WIDTH, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleIntegerWithinRange() throws IOException {

    String json = "{ \"min\":1, \"max\":10 }";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.RANDOM_INTEGER_WITHIN_RANGE, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleIntegerFixedValue() throws JsonProcessingException {
    String json = "{ \"value\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.INTEGER_FIXED_VALUE, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleFloatFixedValue() throws JsonProcessingException {
    String json = "{ \"value\":3.5}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("interest", MaskType.FLOAT_FIXED_VALUE, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleNumericRange() throws JsonProcessingException {
    String json = "{ \"step\":100}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.NUMERIC_RANGE, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleIntegerRange() throws JsonProcessingException {
    String json = "{ \"step\":10}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.INTEGER_RANGE, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleBigIntRange() throws JsonProcessingException {
    String json = "{ \"step\":1000}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.BIGINT_RANGE, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleCardFirst() throws JsonProcessingException {
    String json = "{ \"separator\":\"-\", \"val1\":5, \"val2\":0,}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("card", MaskType.CREDIT_CARD_SHOW_FIRST, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleCardLast() throws JsonProcessingException {
    String json = "{ \"separator\":\"-\", \"val1\":0, \"val2\":5,}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("card", MaskType.CREDIT_CARD_SHOW_LAST, maskParams);
    return createMaskRule(columnRule);
  }

  public static List<MaskingRule> getMaskRuleCardFirstLast() throws JsonProcessingException {
    String json = "{ \"separator\":\"-\", \"val1\":3, \"val2\":3,}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("card", MaskType.CREDIT_CARD_SHOW_FIRST_LAST, maskParams);
    return createMaskRule(columnRule);
  }

}

