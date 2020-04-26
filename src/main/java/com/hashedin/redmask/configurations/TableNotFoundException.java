package com.hashedin.redmask.configurations;

public class TableNotFoundException extends RuntimeException {
  public TableNotFoundException(String tableName) {
    super(String.format("{} was not found.",tableName));
  }
}
