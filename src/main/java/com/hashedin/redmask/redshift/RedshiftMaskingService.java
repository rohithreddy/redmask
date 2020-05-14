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

public class RedshiftMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(RedshiftMaskingService.class);

  private final MaskConfiguration config;
  private String url = "jdbc:redshift://";
  private final boolean dryRunEnabled;

  // This temp would store queries to create masked data for when dryrun is true.
  private final File tempFilePath;

  public RedshiftMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    super();
    this.config = config;
    this.url = url + config.getHost() + ":"
        + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = createMaskingSqlFile();
  }

  @Override
  public void generateSqlQueryForMasking() {
    try {
      FileWriter writer = new FileWriter(tempFilePath);
      log.info("Creating or replacing existing table view.");
      createQueryForFunctionSchema(writer, config.getUser());

      Properties connectionProps = new Properties();
      connectionProps.setProperty("user", config.getSuperUser());
      connectionProps.setProperty("password", config.getSuperUserPassword());
      try {
        Class.forName("com.amazon.redshift.jdbc.Driver");
      } catch (ClassNotFoundException ex) {
        writer.close();
        throw new RedmaskRuntimeException("Driver not found.", ex);
      }
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
        // TODO remove tempQueriesList when possible
        buildFunctionsAndQueryForView(rule, writer, config, url, connectionProps);
      }

      // Grant access of this masked view to user.
      grantAccessToMaskedData(writer, config.getUser());
      writer.flush();
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while writing to file {}", tempFilePath.getName()), ex);
    }
    log.info("Sql script file exists at: {}. You can review it.", tempFilePath);
  }

  @Override
  public void executeSqlQueryForMasking() throws IOException, ClassNotFoundException {
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
      Properties connectionProps = new Properties();
      connectionProps.setProperty("user", config.getSuperUser());
      connectionProps.setProperty("password", config.getSuperUserPassword());
      executeSqlScript(url, connectionProps, tempFilePath);
    }
  }

}
