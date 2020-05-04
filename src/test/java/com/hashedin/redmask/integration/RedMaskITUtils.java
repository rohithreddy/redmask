package com.hashedin.redmask.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.hashedin.redmask.integration.BaseITPostgresTestContainer.TABLE_NAME_2;

public class RedMaskITUtils {

  private static final String TABLE_NAME = "customer"; 

  public static List<MaskingRule> createMaskingRuleVersionOne() throws IOException {
    MaskingRule rule = new MaskingRule();
    rule.setTable(TABLE_NAME);
    List<ColumnRule> columns = new ArrayList<ColumnRule>();
    columns.add(getMaskRuleString("name"));
    columns.add(getMaskRuleEmailFirstCharDomain("email"));
    columns.add(getMaskRuleIntegerWithinRange("age"));
    columns.add(getMaskRuleFloatFixedValue("interest"));
    columns.add(getMaskRuleCardFirstLast("card"));
    rule.setColumns(columns);
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    return maskingRuleList;
  }

  public static List<MaskingRule> createMaskingRuleVersionTwo(){
    MaskingRule rule = new MaskingRule();
    rule.setTable("Invalid"+TABLE_NAME);
    List<ColumnRule> columns = new ArrayList<ColumnRule>();
    rule.setColumns(columns);
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    return maskingRuleList;
  }

  public static List<MaskingRule> createMaskingRuleVersionThree() throws IOException {
    MaskingRule rule = new MaskingRule();
    rule.setTable(TABLE_NAME);
    List<ColumnRule> columns = new ArrayList<ColumnRule>();
    rule.setColumns(columns);
    columns.add(getMaskRuleIntegerWithinRange("bought"));
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    return maskingRuleList;

  }
  public static List<MaskingRule> createMaskingRuleVersionFour() throws JsonProcessingException {
    MaskingRule rule = new MaskingRule();
    rule.setTable(TABLE_NAME);
    List<ColumnRule> columns = new ArrayList<ColumnRule>();
    rule.setColumns(columns);
    String json = "{ \"size\":-1}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.RANDOM_INTEGER_FIXED_WIDTH,
        maskParams);
    columns.add(columnRule);
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    return maskingRuleList;
  }
  public static List<MaskingRule> createMaskingRuleVersionFive() throws JsonProcessingException {
    MaskingRule rule = new MaskingRule();
    rule.setTable(TABLE_NAME);
    List<ColumnRule> columns = new ArrayList<ColumnRule>();
    rule.setColumns(columns);
    String json = "{ \"step\":4}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule = new ColumnRule("age", MaskType.RANDOM_INTEGER_FIXED_WIDTH,
        maskParams);
    columns.add(columnRule);
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    return maskingRuleList;
  }

  public static List<MaskingRule> createMaskingRuleVersionSix() throws IOException {
    MaskingRule rule1 = new MaskingRule();
    rule1.setTable(TABLE_NAME);
    List<ColumnRule> columns = new ArrayList<ColumnRule>();
    columns.add(getMaskRuleIntegerWithinRange("age"));
    rule1.setColumns(columns);
    MaskingRule rule2 = new MaskingRule();
    rule2.setTable(TABLE_NAME_2);
    List<ColumnRule> columns2 = new ArrayList<ColumnRule>();
    columns2.add(getMaskRuleString("name"));
    rule2.setColumns(columns2);
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule1);
    maskingRuleList.add(rule2);
    return maskingRuleList;
  }

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

