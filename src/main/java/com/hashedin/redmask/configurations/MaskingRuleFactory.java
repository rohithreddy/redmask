package com.hashedin.redmask.configurations;

import com.hashedin.redmask.service.MaskingRuleDef;

public class MaskingRuleFactory {

  public MaskingRuleDef getColumnMaskingRule(MaskingRuleDef ruleDef) 
      throws IllegalAccessException, InstantiationException {

    MaskingRuleDef specificRule = ruleDef.getMaskType().getClassType().newInstance();
    specificRule.setColumnName(ruleDef.getColumnName());
    specificRule.setMaskType(ruleDef.getMaskType());
    specificRule.setMaskParams(ruleDef.getMaskParams());
    return specificRule;
  }

}