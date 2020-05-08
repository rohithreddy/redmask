package com.hashedin.redmask.common;

public class QueryBuilder {

  private static final String NEW_LINE = System.getProperty("line.separator");

  private QueryBuilder() {}
  /**
   * It generates the SQL query to drops the schema if it already exists.
   *
   * @param schemaName The name of the schema to be dropped.
   * @return SQL query to drop the intended schema.
   */
  public static String dropSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
      .append("-- Drop " + schemaName + "Schema if it exists.")
      .append(NEW_LINE);

    sb.append("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;")
      .append(NEW_LINE);
    return sb.toString();
  }
  
  /**
   * It generates the SQL query in order to create a new schema.
   *
   * @param schemaName The name of the schema to be created.
   * @return The SQL query to create the intended schema.
   */
  public static String createSchemaQuery(String schemaName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEW_LINE)
        .append("-- Create " + schemaName + " schema.")
        .append(NEW_LINE);

    sb.append("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";")
        .append(NEW_LINE);
    return sb.toString();
  }
}
