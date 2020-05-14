package com.hashedin.redmask.snowflake;

import com.hashedin.redmask.common.DataMasking;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.exception.RedmaskRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class SnowflakeMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(SnowflakeMaskingService.class);
  private static final String SNOWFLAKE_JDBC_DRIVER = "net.snowflake.client.jdbc.SnowflakeDriver";


  private final MaskConfiguration config;
  private final boolean dryRunEnabled;
  private String url = "jdbc:snowflake://";

  /**
   *  This Temp file would store SQL queries/script to create schema, masked views,
   *  grant permission to user etc. 
   */
  private final File tempFilePath;

  public SnowflakeMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    this.config = config;
    this.url = url + config.getHost();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = createMaskingSqlFile();
    log.trace("Initialized Snowflake masking service.");
  }

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
          String.format("Error while writing to file {}", tempFilePath.getName()), ex);
    }
    log.info("Sql script file exists at: {}. It contains all the sql queries"
        + "needed to create masked data.", tempFilePath);
  }

  @Override
  public void executeSqlQueryForMasking() throws IOException {
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
    }
  }

}
