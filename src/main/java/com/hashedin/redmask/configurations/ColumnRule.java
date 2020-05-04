package com.hashedin.redmask.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ColumnRule {

  @JsonProperty("name")
  private String columnName; 

  //Value from MaskType enum.
  @JsonProperty("mask_type")
  private MaskType maskType; 

  // A map(key, value) of additional parameters needed for this masking rule.
  @JsonProperty("mask_params")
  private JsonNode maskParams;

  public ColumnRule() {}

  /**
   * This function is only be used while writing integration test case to generate masking
   * rule on a column
   * @param columnName The column on which the mask is to be applied on.
   * @param maskType The type of masking function to be used to mask the data.
   * @param maskParams Additional configuration parameters for the mask type.
   */
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
