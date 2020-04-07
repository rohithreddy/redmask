package com.hashedin.redmask.configurations;

public class ColumnRule {

  private String name;
  private MaskType rule;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MaskType getRule() {
    return rule;
  }

  public void setRule(MaskType rule) {
    this.rule = rule;
  }

  @Override
  public String toString() {
    return "ColumnRule [name=" + name + ", rule=" + rule + "]";
  }
}
