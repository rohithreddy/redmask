package com.hashedin.redmask.configurations;

public class ColumnNotFoundException extends RuntimeException {
  public ColumnNotFoundException(String columnName, String tableName) {
    super(String.format("{} was not found in {} table.", columnName, tableName));
  }
}
