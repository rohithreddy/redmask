package com.hashedin.redmask.redshift;

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


public class RedshiftMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(RedshiftMaskingService.class);

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

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
    this.tempFilePath = QueryBuilderUtil.createMaskingSqlFile();
  }

  @Override
  public void generateSqlQueryForMasking() {
    try {
      FileWriter writer = new FileWriter(tempFilePath);
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
  public void executeSqlQueryForMasking() throws IOException, ClassNotFoundException {
    //TODO implement execute query for redshift from temp query file.
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
    }
  }

}
