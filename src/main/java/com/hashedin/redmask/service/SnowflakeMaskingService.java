package com.hashedin.redmask.service;

import com.hashedin.redmask.common.DataMasking;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.exception.RedmaskRuntimeException;

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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class provides implementation of DataMasking for Snowflake.
 */
public class SnowflakeMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(SnowflakeMaskingService.class);
  private static final String SNOWFLAKE_JDBC_DRIVER = "net.snowflake.client.jdbc.SnowflakeDriver";
  private static final String SNOWFLAKE_DEFAULT_SCHEMA = "PUBLIC";

  private final MaskConfiguration config;
  private final boolean dryRunEnabled;
  private String url = "jdbc:snowflake://";

  /**
   * This Temp file would store SQL queries/script to create schema, masked views,
   * grant permission to user etc.
   */
  private final File tempFilePath;

  public SnowflakeMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    this.config = config;
    this.url = url + config.getHost();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = createMaskingSqlFile();
    log.trace("Initialized Snowflake masking service.");
  }

  /**
   * Steps:
   * <p>1. Create a masking.sql file - This will contain all required masking queries.
   * <p>2. Build queries to create Schema that will contain masking function and masked data.
   * <p>3. Build queries to create masking function for given masking rule.
   * <p>4. Build queries to create masked view using those masking function.
   * <p>5. Revoke access to public schema from user(dev user).
   * <p>6. Provide access to schema that contains masked view to user.
   */
  @Override
  public void generateSqlQueryForMasking() {
    try {
      FileWriter writer = new FileWriter(tempFilePath);
      writer.append("USE " + config.getDatabase() + ";");
      appendQueryToCreateSchema(writer, config.getUser());

      try {
        Class.forName(SNOWFLAKE_JDBC_DRIVER);
      } catch (ClassNotFoundException ex) {
        writer.close();
        throw new RedmaskRuntimeException("Snowflake JDBC driver not found.", ex);
      }

      // Set database superuser credentials 
      Properties connectionProps = new Properties();
      connectionProps.setProperty("user", config.getSuperUser());
      connectionProps.setProperty("password", config.getSuperUserPassword());
      connectionProps.setProperty("db", config.getDatabase());
      connectionProps.setProperty("schema", DEFAULT_INPUT_TABLE_SCHEMA);

      /**
       * For each masking rule, build queries to create masking function.
       * Build queries to create masked view for given table.
       *
       * Append all those queries to file writer.
       */
      for (int i = 0; i < config.getRules().size(); i++) {
        MaskingRule rule = config.getRules().get(i);
        buildQueryAndAppend(rule, writer, config, url, connectionProps);
      }

      // TODO add required permission to grant access to different users
      // Grant access to the masked view data to user.
      grantAccessToMaskedData(writer, config.getUser());
      writer.flush();
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while writing to file '%s'", tempFilePath.getName()), ex);
    }
    log.info("Sql script file exists at: {}. It contains all the sql queries"
        + "needed to create masked data.", tempFilePath);
  }

  @Override
  public void executeSqlQueryForMasking() {
    if (!dryRunEnabled) {
      log.trace("Invoking Redshift masking service to run data masking sql script.");
      Properties connectionProps = new Properties();
      connectionProps.setProperty("user", config.getSuperUser());
      connectionProps.setProperty("password", config.getSuperUserPassword());
      connectionProps.setProperty("db", config.getDatabase());
      connectionProps.setProperty("schema", SNOWFLAKE_DEFAULT_SCHEMA);
      try {
        executeSqlScript(url, connectionProps, tempFilePath);
      } catch (IOException ex) {
        throw new RedmaskRuntimeException(ex);
      }
    }
  }

  @Override
  protected void executeSqlScript(String url, Properties props,
                                  File scriptFilePath) throws IOException {
    Reader reader = null;
    try (Connection CONN = DriverManager.getConnection(url, props)) {
      //Initialize the script runner
      ScriptRunner sr = new ScriptRunner(CONN);

      //Creating a reader object and running the script
      reader = new BufferedReader(new FileReader(scriptFilePath));
      sr.setSendFullScript(false);
      log.trace("Executing sql script located at {}", scriptFilePath);
      sr.runScript(reader);
    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while executing masking sql "
                  + "query from file: %s using super username: %s",
              scriptFilePath, props.get("user")), ex);
    } catch (FileNotFoundException ex) {
      throw new RedmaskRuntimeException(
          String.format("Masking sql query file '%s' not found", scriptFilePath.getName()), ex);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  @Override
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
      writer.append("\nGRANT ALL PRIVILEGES ON ALL VIEWS IN "
          + "SCHEMA " + user + " TO " + user + ";");
      writer.append("\nGRANT USAGE ON SCHEMA " + user
          + " TO " + user + ";");
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          "Erorr while appending sql query to grant access to maksed data in temp file.", ex);
    }
  }

}
