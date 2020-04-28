package com.hashedin.redmask.configurations;

import com.hashedin.redmask.service.MaskingRuleDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory class is used to create an instance of a specific masking rule from the inputted
 * column rules.
 */
public class MaskingRuleFactory {

  private static final Logger log = LoggerFactory.getLogger(MaskingRuleFactory.class);
  
  public MaskingRuleDef getColumnMaskingRule(MaskingRuleDef ruleDef) {

    MaskingRuleDef specificRule = null;
    try {
      specificRule = ruleDef.getMaskType().getClassType().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      log.error("Invalid MaskType or function class specified", e);
    }
    specificRule.setColumnName(ruleDef.getColumnName());
    specificRule.setMaskType(ruleDef.getMaskType());
    specificRule.setMaskParams(ruleDef.getMaskParams());
    return specificRule;
  }

}