package com.hashedin.redmask.exception;

public class ColumnNotFoundException extends RuntimeException {
  public ColumnNotFoundException(String columnName, String tableName) {
    super(String.format("{} was not found in {} table.", columnName, tableName));
  }
}
