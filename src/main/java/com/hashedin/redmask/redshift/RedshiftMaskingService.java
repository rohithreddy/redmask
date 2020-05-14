package com.hashedin.redmask.redshift;

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
 * This class provides implementation of DataMasking for Redshift.
 */
public class RedshiftMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(RedshiftMaskingService.class);
  private static final String REDSHIFT_JDBC_DRIVER = "com.amazon.redshift.jdbc.Driver";

  private final MaskConfiguration config;
  private final boolean dryRunEnabled;
  private String url = "jdbc:redshift://";

  /**
   *  This Temp file would store SQL queries/script to create schema, masked views,
   *  grant permission to user etc. 
   */
  private final File tempFilePath;

  public RedshiftMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    this.config = config;
    this.url = url + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = createMaskingSqlFile();
    log.trace("Initialized Redshift masking service.");
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
      log.trace("Invoking Redshift masking service to generate data masking sql script.");
      FileWriter writer = new FileWriter(tempFilePath);
      appendQueryToCreateSchema(writer, config.getUser());

      try {
        Class.forName(REDSHIFT_JDBC_DRIVER);
      } catch (ClassNotFoundException ex) {
        writer.close();
        throw new RedmaskRuntimeException("Redshift JDBC driver not found.", ex);
      }
      
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

  @Override
  public void executeSqlQueryForMasking() {
    if (!dryRunEnabled) {
      log.trace("Invoking Redshift masking service to run data masking sql script.");
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
