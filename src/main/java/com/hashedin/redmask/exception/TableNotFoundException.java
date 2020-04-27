package com.hashedin.redmask.exception;

public class TableNotFoundException extends RuntimeException {
  public TableNotFoundException(String tableName) {
    super(String.format("{} was not found.", tableName));
  }
}
