package com.hashedin.redmask.postgres;

import com.hashedin.redmask.common.MaskingQueryUtil;
import com.hashedin.redmask.common.QueryBuilderUtil;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import com.hashedin.redmask.factory.DataMasking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PostgresMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(PostgresMaskingService.class);

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

  private final MaskConfiguration config;
  private String url = "jdbc:postgresql://";
  private final boolean dryRunEnabled;

  // This temp would contain queries to create masked data.
  private final File tempFilePath;

  public PostgresMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    this.config = config;
    this.url = url + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = QueryBuilderUtil.createMaskingSqlFile();
  }

  /**
   * Steps:
   * <p>
   * Create a masking.sql file. This will contain all required masking queries.
   * Create Schema.
   * Create masking function for given masking rule.
   * Create View using those masking function.
   * Provide access to user to read data from masked view.
   */
  @Override
  public void generateSqlQueryForMasking() throws RedmaskConfigException {

    try {
      FileWriter writer = new FileWriter(tempFilePath);

      /*
       * Drop and Create redmask Schema and user schema.
       * TODO :Find a better way without dropping schema.
       */
      log.info("Creating or replacing existing table view.");
      writer.append(MaskingQueryUtil.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(MaskingQueryUtil.dropSchemaQuery(config.getUser()));
      writer.append(MaskingQueryUtil.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(MaskingQueryUtil.createSchemaQuery(config.getUser()));

      /**
       * For each masking rule, create postgres mask function.
       * Create view for given table.
       *
       * First generate query for creating function query for all the masking rule needed.
       * Then we can generate query for creating masked view
       */
      // Generate query for each table and append in the writer.
      log.info("Adding function and custom queries to build view.");
      for (int i = 0; i < config.getRules().size(); i++) {
        MaskingRule rule = config.getRules().get(i);
        QueryBuilderUtil.buildFunctionsAndQueryForView(rule, writer, config, url);
      }

      // Grant access of this masked view to user.
      grantAccessToMaskedData(writer, MASKING_FUNCTION_SCHEMA, config.getUser());
      writer.flush();
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while writing to file {}", tempFilePath.getName()), ex);
    }
  }

  @Override
  public void executeSqlQueryForMasking() throws IOException {
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
      Properties connectionProps = new Properties();
      connectionProps.setProperty("user", config.getSuperUser());
      connectionProps.setProperty("password", config.getSuperUserPassword());
      executeSqlScript(url, connectionProps, tempFilePath);
    }
  }

}
