package com.hashedin.redmask.configurations;

public class MaskingRuleFactory {

  public ColumnRule getColumnMaskingRule(ColumnRule columnRule) 
      throws IllegalAccessException, InstantiationException {

    ColumnRule specificRule = columnRule.getMaskType().getClassType().newInstance();
    specificRule.setName(columnRule.getName());
    specificRule.setMaskType(columnRule.getMaskType());
    specificRule.setMaskParams(columnRule.getMaskParams());
    return specificRule;
  }

}