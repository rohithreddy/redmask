package com.hashedin.redmask.config;

import java.util.List;

public class MaskingRule {

  private String table;
  private List<ColumnRule> columns;

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public List<ColumnRule> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnRule> columns) {
    this.columns = columns;
  }

  @Override
  public String toString() {
    return "MaskingRule [table=" + table + ", columns=" + columns + "]";
  }

}
