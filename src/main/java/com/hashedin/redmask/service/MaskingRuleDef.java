package com.hashedin.redmask.service;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a abstract class which contains parameters and function 
 * needed to implement/construct masking function definition.
 * 
 * All Specific masking function class needs to extend this class.
 */
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

  public abstract String getSubQuery(MaskConfiguration config, String tableName)
      throws IOException, TemplateException;

  protected abstract boolean validateAndAddParameters(List<String> parameters);

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
