package com.hashedin.redmask.postgres;

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

/**
 * This class provides implementation of DataMasking for Postgres.
 */
public class PostgresMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(PostgresMaskingService.class);

  private final MaskConfiguration config;
  private final boolean dryRunEnabled;
  private String url = "jdbc:postgresql://";

  /**
   *  This Temp file would store SQL queries/script to create schema, masked views,
   *  grant permission to user etc. 
   */
  private final File tempFilePath;

  public PostgresMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    this.config = config;
    this.url = url + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = createMaskingSqlFile();
    log.trace("Initialized Postgres masking service.");
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
      appendQueryToCreateSchema(writer, config.getUser());
      
      // Set database superuser credentials
      Properties connectionProps = new Properties();
      connectionProps.setProperty("user", config.getSuperUser());
      connectionProps.setProperty("password", config.getSuperUserPassword());

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

  /**
   * Run the SQL script present in Temp file created earlier.
   */
  @Override
  public void executeSqlQueryForMasking() {
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
      Properties connectionProps = new Properties();
      connectionProps.setProperty("user", config.getSuperUser());
      connectionProps.setProperty("password", config.getSuperUserPassword());
      try {
        executeSqlScript(url, connectionProps, tempFilePath);
      } catch (IOException ex) {
        throw new RedmaskRuntimeException(ex);
      }
    }
  }

}
