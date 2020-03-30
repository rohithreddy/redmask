package com.hashedin.redmask.configurations;

public class MaskingRule {

  private String table;
  
  private String column;
  
  private MaskType rule;
  
  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
  }

  public MaskType getRule() {
    return rule;
  }

  public void setRule(MaskType rule) {
    this.rule = rule;
  }

  @Override
  public String toString() {
    return "MaskingRule [table=" + table + ", column=" + column + ", rule=" + rule + "]";
  }

}
