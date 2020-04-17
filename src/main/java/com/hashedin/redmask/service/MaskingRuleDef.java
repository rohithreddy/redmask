package com.hashedin.redmask.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;

import freemarker.template.TemplateException;

public abstract class MaskingRuleDef {

  private String columnName; 

  // Value from MaskType enum.
  private MaskType maskType; 

  // A map(key, value) of additional parameters needed for this masking rule.
  private Map<String, String> maskParams;

  public MaskingRuleDef(String columnName, MaskType maskType, Map<String, String> maskParams) {
    this.columnName = columnName;
    this.maskType = maskType;
    this.maskParams = maskParams;
  }

  public MaskingRuleDef() {}

  public abstract void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException;

  public abstract String getSubQuery(String tableName);

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public MaskType getMaskType() {
    return maskType;
  }

  public void setMaskType(MaskType maskType) {
    this.maskType = maskType;
  }

  public Map<String, String> getMaskParams() {
    return maskParams;
  }

  public void setMaskParams(Map<String, String> maskParams) {
    this.maskParams = maskParams;
  }

}
