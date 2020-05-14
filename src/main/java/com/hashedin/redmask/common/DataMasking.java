package com.hashedin.redmask.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.config.ColumnRule;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.config.TemplateConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import com.hashedin.redmask.factory.DataBaseType;
import com.hashedin.redmask.factory.MaskingRuleFactory;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This DataMasking class contains common methods to implement data masking.
 * 
 * Any Specific DataBase/Data Warehouse class implementing data masking 
 * has to extend this class and provide implementation to abstract methods.
 */
public abstract class DataMasking {

  private static final Logger log = LoggerFactory.getLogger(DataMasking.class);
  public static final String MASKING_FUNCTION_SCHEMA = "redmask";
  public static final String SELECT_QUERY = "SELECT * FROM ";
  public static final String DEFAULT_INPUT_TABLE_SCHEMA = "public";

  /**
   * Steps:
   * <p>1. Create a masking.sql file - This will contain all required masking queries.
   * <p>2. Build queries to create Schema that will contain masking function and masked data.
   * <p>3. Build queries to create masking function for given masking rule.
   * <p>4. Build queries to create masked view using those masking function.
   * <p>5. Revoke access to public schema from user(dev user).
   * <p>6. Provide access to schema that contains masked view to user.
   */
  public abstract void generateSqlQueryForMasking();

  /**
   * Run the SQL script present in .sql temp file.
   */
  public abstract void executeSqlQueryForMasking();

  /**
   * This method builds all the needed query to create masking functions and views.
   * @param rule    MaskingRule rule object.
   * @param writer  FileWriter object. All the queries are appended to this writer.
   * @param config  MaskConfiguration object
   * @param url     database url.
   * @param connectionProps  DB connection properties.
   */
  public void buildQueryAndAppend(MaskingRule rule, FileWriter writer,
      MaskConfiguration config, String url, Properties connectionProps) {

    Set<String> functionDefinitionSet = new LinkedHashSet<>();
    List<String> querySubstring = new ArrayList<>();
    ResultSetMetaData rs = null;
    TemplateConfiguration templateConfig = config.getTemplateConfig();

    try (Connection CONN = DriverManager.getConnection(url, connectionProps);
         Statement STATEMENT = CONN.createStatement()) {

      // Check if given table exists.
      if (!isValidTable(CONN, rule.getTable())) {
        throw new RedmaskConfigException(String.format("Table {} was not found.", rule.getTable()));
      }

      String query = SELECT_QUERY + rule.getTable();
      rs = STATEMENT.executeQuery(query).getMetaData();
      MaskingRuleFactory maskRuleFactory = new MaskingRuleFactory();

      log.info("Storing masking function names required to create the intended masked view.");
      // Create a map of column and their associated masking rule.
      Map<String, MaskingRuleDef> colMaskRuleMap = new HashMap<>();

      createColumnMaskRuleMap(rule, CONN, maskRuleFactory, colMaskRuleMap);
      getColumnMaskSubQueries(rule, functionDefinitionSet, querySubstring, rs, templateConfig,
          colMaskRuleMap, config.getDbType());
    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(ex);
    }

    /**
     * Inserting Masking function definition to the temporary redmask-masking.sql file.
     * Appending each masking function to redmask-masking.sql file
     */
    for (String functionDefinition : functionDefinitionSet) {
      try {
        writer.append(functionDefinition);
      } catch (IOException e) {
        throw new RedmaskRuntimeException(
            "Exception while appending masking function definition to file writer", e);
      }
    }

    // Create view query.
    log.info("Creating the query in order to create the intended masked view.");
    String queryString = String.join(",", querySubstring);
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(rule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT "
        + queryString + " FROM ";
    if (config.getDbType().toString().equalsIgnoreCase(DataBaseType.SNOWFLAKE.toString())) {
      createViewQuery = createViewQuery + "public." + rule.getTable() + ";";
    } else {
      createViewQuery = createViewQuery + rule.getTable() + ";";
    }

    try {
      writer.append("\n\n-- Create masked view.\n");
      writer.append(createViewQuery);

      writer.append("\n\n-- Revoking access from original table");
      writer.append("\nREVOKE ALL ON TABLE " + DEFAULT_INPUT_TABLE_SCHEMA + "."
          + rule.getTable() + " FROM " + config.getUser() + ";");
    } catch (IOException e) {
      throw new RedmaskRuntimeException(
          "Exception while appending masking function definition to file writer", e);
    }

  }

  /**
   * Creates a map of the column name and the associated masking rule.
   *
   * @param rule              The masking rule given for a table.
   * @param connection        Connection object to the SQL database
   * @param columnRuleFactory Factory class in order to convert the column rule to a specific
   *                          rule class
   * @param colMaskRuleMap    Map containing the column name as key and the specific rule class as
   *                          value.
   */
  protected void createColumnMaskRuleMap(
      MaskingRule rule,
      Connection connection,
      MaskingRuleFactory columnRuleFactory,
      Map<String, MaskingRuleDef> colMaskRuleMap) {

    // For each column rule in a given table.
    for (ColumnRule col : rule.getColumns()) {
      try {
        if (!isValidColumn(connection, rule.getTable(), col.getColumnName())) {
          throw new RedmaskConfigException(
              String.format("Column {} was not found in {} table.",
                  col.getColumnName(), rule.getTable()));
        }
      } catch (SQLException ex) {
        throw new RedmaskRuntimeException(
            String.format("Error while fetching column detail for Column {} in table {}.",
                col.getColumnName(), rule.getTable()), ex);
      }
      // Build MaskingRuleDef object.
      MaskingRuleDef ruleDef = buildMaskingRuleDef(col);
      colMaskRuleMap.put(col.getColumnName(), columnRuleFactory.getColumnMaskingRule(ruleDef));
    }
  }

  /**
   * Creates a temp .sql file to store SQL script on local machine/server.
   * @return Path to the created new temp file.
   */
  protected File createMaskingSqlFile() {
    File sqlFile = null;
    try {
      sqlFile = File.createTempFile("redmask-masking", ".sql");
      log.info("Created a temp file at location: {}", sqlFile.getAbsolutePath());
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          "Error while creating a temporary file \'redmask-masking.sql\'", ex);
    }
    return sqlFile;
  }
  
  /**
   * This function add all the dynamically design sub-queries based on the columns dn the mask type
   * and adds it into the querySubstring list.
   *
   * @param rule                  The masking rule for a given table.
   * @param functionDefinitionSet A set to store the unique function definition to be created to
   *                              execute the necessary masked view.
   * @param querySubstring        A list to store sub-queries built dynamically for making the
   *                              view query.
   * @param rs                    The ResultSet object contain the metadata for column of the table
   *                              specified in the
   *                              masking rule.
   * @param templateConfig        Template Configuration in order to access the different templates
   *                              to create user specific function definition and sub-queries
   * @param colMaskRuleMap        Map of the column to be masked and their masking function
   */
  protected void getColumnMaskSubQueries(MaskingRule rule, Set<String> functionDefinitionSet,
      List<String> querySubstring, ResultSetMetaData rs, TemplateConfiguration templateConfig,
      Map<String, MaskingRuleDef> colMaskRuleMap, DataBaseType dbType) {
    // For each column in a given table.
    try {
      for (int i = 1; i <= rs.getColumnCount(); i++) {
        String colName = rs.getColumnName(i);
        // Check if masking has to be done for this column.
        if (colMaskRuleMap.containsKey(colName)) {
          querySubstring.add(colMaskRuleMap.get(colName)
              .getSubQuery(templateConfig, rule.getTable()));
          String databaseType = dbType.toString().toLowerCase();
          colMaskRuleMap.get(colName)
            .addFunctionDefinition(templateConfig, functionDefinitionSet, databaseType);
        } else {
          querySubstring.add(rs.getColumnName(i));
        }
      }
    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(ex);
    }
  }

  protected void appendQueryToCreateSchema(FileWriter writer, String userSchema) {
    log.trace("Appending sql query to drop and create schema in temp file."
        + "Schema to be created are {}, {}", MASKING_FUNCTION_SCHEMA, userSchema);
    try {
      writer.append(MaskingQueryUtil.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(MaskingQueryUtil.dropSchemaQuery(userSchema));
      writer.append(MaskingQueryUtil.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(MaskingQueryUtil.createSchemaQuery(userSchema));
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          "Erorr while appending sql query to drop and create schema in temp file.", ex);
    }
  }

  protected void grantAccessToMaskedData(FileWriter writer, String user) {
    log.info("Appending sql query to provide required permission"
        + "masked data to the user: {}.", user);
    try {
      writer.append("\n\n-- Grant access to current user on schema: "
          + MASKING_FUNCTION_SCHEMA + ".\n");
      writer.append("GRANT USAGE ON SCHEMA " + MASKING_FUNCTION_SCHEMA
          + " TO " + user + ";");
      writer.append("\n\n-- Grant access to current user on schema: " + user + ".\n");
      writer.append("GRANT ALL PRIVILEGES ON ALL TABLES IN "
          + "SCHEMA " + user + " TO " + user + ";");
      writer.append("\nGRANT USAGE ON SCHEMA " + user
          + " TO " + user + ";");
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          "Erorr while appending sql query to grant access to maksed data in temp file.", ex);
    }
  }

  /**
   * This function check whether a table exists in the connected Database.
   *
   * @param connection Connection Object to the intended Database.
   * @param tableName  The name of the table to be checked.
   * @return True, if the Table is present in the database else false.
   */
  private boolean isValidTable(Connection connection, String tableName) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet rs = metaData.getTables(null, null, tableName, null);
    if (rs.next()) {
      rs.close();
      return true;
    }
    rs.close();
    return false;
  }

  /**
   * This function check whether a column exists in a particular table.
   *
   * @param connection Connection Object to the intended Database.
   * @param tableName  The name of the table that should contain the column.
   * @param columnName The name of the column to be checked.
   * @return True, if the column is present in the table else false.
   */
  private boolean isValidColumn(Connection connection, String tableName,
                                           String columnName) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
    if (rs.next()) {
      rs.close();
      return true;
    }
    rs.close();
    return false;
  }

  private MaskingRuleDef buildMaskingRuleDef(ColumnRule colRule) {
    Map<String, String> maskParams = new ObjectMapper().convertValue(
        colRule.getMaskParams(), new TypeReference<Map<String, String>>() {
        });

    return new MaskingRuleDef(colRule.getColumnName(),
        colRule.getMaskType(), maskParams) {

      @Override
      public void addFunctionDefinition(
          TemplateConfiguration config,
          Set<String> funcSet, String dbType) {
      }

      @Override
      public String getSubQuery(TemplateConfiguration config, String tableName) {
        return "";
      }
    };
  }
  
  protected void executeSqlScript(String url, Properties props,
      File scriptFilePath) throws IOException {
    Reader reader = null;
    try (Connection CONN = DriverManager.getConnection(url, props)) {
      //Initialize the script runner
      ScriptRunner sr = new ScriptRunner(CONN);

      //Creating a reader object and running the script
      reader = new BufferedReader(new FileReader(scriptFilePath));
      sr.setSendFullScript(true);
      log.trace("Executing sql script located at {}", scriptFilePath);
      sr.runScript(reader);
    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while executing masking sql "
              + "query from file: {} using super username: {}",
              scriptFilePath, props.get("user")), ex);
    } catch (FileNotFoundException ex) {
      throw new RedmaskRuntimeException(
          String.format("Masking sql query file {} not found", scriptFilePath.getName()), ex);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

}
