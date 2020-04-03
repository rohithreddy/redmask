package com.hashedin.redmask.service;

public class BuildQueryUtil {

  private final static String newLine = System.getProperty("line.separator");
  
  public static String dropSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(newLine)
      .append("-- Drop " + schemaName + "Schema if it exists.")
      .append(newLine);
    
    sb.append("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;")
      .append(newLine);
    return sb.toString();
  }
  
  public static String createSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(newLine)
      .append("-- Create " + schemaName + " schema.")
      .append(newLine);
    
    sb.append("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";")
      .append(newLine);
    return sb.toString();
  }

}
