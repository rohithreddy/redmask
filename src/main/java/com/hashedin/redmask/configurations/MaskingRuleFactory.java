package com.hashedin.redmask.configurations;

import com.hashedin.redmask.service.MaskingRuleDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MaskingRuleFactory {

  private static final Logger log = LogManager.getLogger(MaskingRuleFactory.class);

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