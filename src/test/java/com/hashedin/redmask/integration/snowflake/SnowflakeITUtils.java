package com.hashedin.redmask.integration.snowflake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.config.ColumnRule;
import com.hashedin.redmask.config.MaskType;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.integration.RedMaskITUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class SnowflakeITUtils extends RedMaskITUtils {

  private static final String TABLE_NAME = "CUSTOMER";
  private static final String TABLE_NAME_2 = "CASHIER";

  public static List<MaskingRule> createMaskingRuleVersionOne() throws IOException {
    MaskingRule rule = new MaskingRule();
    rule.setTable(TABLE_NAME);
    List<ColumnRule> columns = new ArrayList<ColumnRule>();
    columns.add(getMaskRuleString("NAME"));
    columns.add(getMaskRuleEmailFirstCharDomain("EMAIL"));
    columns.add(getMaskRuleIntegerWithinRange("AGE"));
    columns.add(getMaskRuleFloatFixedValue("INTEREST"));
    columns.add(getMaskRuleCardFirstLast("CARD"));
    rule.setColumns(columns);
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    return maskingRuleList;
  }

  public static List<MaskingRule> createMaskingRuleVersionTwo() {
    MaskingRule rule = new MaskingRule();
    rule.setTable("Invalid" + TABLE_NAME);
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
    columns.add(getMaskRuleIntegerWithinRange("BOUGHT"));
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
    ColumnRule columnRule = new ColumnRule("AGE", MaskType.RANDOM_INTEGER_FIXED_WIDTH,
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
    ColumnRule columnRule = new ColumnRule("AGE", MaskType.RANDOM_INTEGER_FIXED_WIDTH,
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
    columns.add(getMaskRuleIntegerWithinRange("AGE"));
    rule1.setColumns(columns);
    MaskingRule rule2 = new MaskingRule();
    rule2.setTable(TABLE_NAME_2);
    List<ColumnRule> columns2 = new ArrayList<ColumnRule>();
    columns2.add(getMaskRuleString("NAME"));
    rule2.setColumns(columns2);
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule1);
    maskingRuleList.add(rule2);
    return maskingRuleList;
  }

}

