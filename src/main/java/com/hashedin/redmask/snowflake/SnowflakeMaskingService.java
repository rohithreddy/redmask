package com.hashedin.redmask.snowflake;

import com.hashedin.redmask.common.MaskingQueryUtil;
import com.hashedin.redmask.common.QueryBuilderUtil;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import com.hashedin.redmask.factory.DataMasking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnowflakeMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(SnowflakeMaskingService.class);

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

  private final MaskConfiguration config;
  private String url = "jdbc:snowflake://";
  private final boolean dryRunEnabled;
  // TODO remove the tempQueriesList when logic for running script is added
  // This temp list will contain all the individual queries.
  private List<String> tempQueriesList;


  // This temp would store queries to create masked data for when dryrun is true.
  private final File tempFilePath;

  public SnowflakeMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    super();
    this.config = config;
    this.url = url + config.getHost();
    this.dryRunEnabled = dryRunEnabled;
    this.tempQueriesList = new ArrayList<String>();
    this.tempFilePath = createMaskingSqlFile();
  }

  @Override
  public void generateSqlQueryForMasking() {
    try {
      FileWriter writer = new FileWriter(tempFilePath);
      log.info("Creating or replacing existing table view.");
      writer.append("USE " + config.getDatabase() + ";");
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
        // TODO remove tempQueriesList when possible
        QueryBuilderUtil.buildFunctionsAndQueryForView(rule, writer, config, url);
      }
      // TODO add required permission to grant access to different users
      writer.flush();
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while writing to file {}", tempFilePath.getName()), ex);
    }
  }

  @Override
  public void executeSqlQueryForMasking() throws IOException {
    //TODO implement execute query for redshift from temp query file.
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
    }
  }

  private File createMaskingSqlFile() {
    // create a temp .sql file
    File sqlFile = null;
    try {
      sqlFile = File.createTempFile("redmask-masking", ".sql");
      log.info("Created a temp file at location: {}", sqlFile.getAbsolutePath());
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          "Error while creating temporary file \'redmask-masking.sql\'", ex);
    }
    return sqlFile;
  }

}
