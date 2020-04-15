package com.hashedin.redmask.configurations;

import com.fasterxml.jackson.databind.JsonNode;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Set;

public class ColumnRule {

  private String name;
  private MaskType maskType;
  private JsonNode maskParams;

  public ColumnRule() {
  }

  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException {
  }

  public String getSubQuery(String tableName){ 
    return null;
  }

  public JsonNode getMaskParams() {
    return maskParams;
  }

  public void setMaskParams(JsonNode maskParams) {
    this.maskParams = maskParams;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MaskType getMaskType() {
    return maskType;
  }

  public void setMaskType(MaskType maskType) {
    this.maskType = maskType;
  }

  @Override
  public String toString() {
    return "ColumnRule [name=" + name + ", maskType=" + maskType + ", maskParams=" + maskParams + "]";
  }

}
