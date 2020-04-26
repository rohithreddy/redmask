package com.hashedin.redmask.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ColumnRule {

  @JsonProperty("name")
  private String columnName; 

  //Value from MaskType enum.
  private MaskType maskType; 

  // A map(key, value) of additional parameters needed for this masking rule.
  private JsonNode maskParams;

  public ColumnRule() {}

  public ColumnRule(String columnName, MaskType maskType, JsonNode maskParams) {
    this.columnName = columnName;
    this.maskType = maskType;
    this.maskParams = maskParams;
  }

  public JsonNode getMaskParams() {
    return maskParams;
  }

  public String getColumnName() {
    return columnName;
  }

  public MaskType getMaskType() {
    return maskType;
  }

  @Override
  public String toString() {
    return "ColumnRule [columnName=" + columnName
        + ", maskType=" + maskType
        + ", maskParams=" + maskParams + "]";
  }

}
