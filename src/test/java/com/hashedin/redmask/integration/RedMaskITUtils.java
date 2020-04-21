package com.hashedin.redmask.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingRule;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RedMaskITUtils {
  public static final MaskConfiguration getMaskConfigurationVersionOne() throws IOException {
    MaskConfiguration config =
        new MaskConfiguration("test", "test", "test", "test");
    String json = "{ \"min\":1, \"max\":10 }";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode maskParams = objectMapper.readTree(json);
    ColumnRule columnRule= new ColumnRule("age", MaskType.RANDOM_INTEGER_WITHIN_RANGE,maskParams);
    MaskingRule rule = new MaskingRule();
    rule.setTable("test_table");
    rule.setColumns(Collections.singletonList(columnRule));
    List<MaskingRule> maskingRuleList = new LinkedList<>();
    maskingRuleList.add(rule);
    config.setRules(maskingRuleList);
    return config;
  }
}

