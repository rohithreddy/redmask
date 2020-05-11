package com.hashedin.redmask.redshift;

import com.hashedin.redmask.common.QueryBuilder;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import com.hashedin.redmask.factory.DataMasking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RedshiftMaskingService extends DataMasking {

  private static final Logger log = LoggerFactory.getLogger(RedshiftMaskingService.class);

  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

  private final MaskConfiguration config;
  private String url = "jdbc:redshift://";
  private final boolean dryRunEnabled;
  // TODO remove the tempQueriesList when logic for running script is added
  // This temp list will contain all the individual queries.
  private List<String> tempQueriesList;


  // This temp would store queries to create masked data for when dryrun is true.
  private final File tempFilePath;

  public RedshiftMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    super();
    this.config = config;
    this.url = url + config.getHost() + ":"
        + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
    this.tempQueriesList = new ArrayList<String>();
    this.tempFilePath = createMaskingSqlFile();
  }

  @Override
  public void generateSqlQueryForMasking() {
    RedshiftQueryBuilder redshiftQueryBuilder = new RedshiftQueryBuilder();
    try {
      FileWriter writer = new FileWriter(tempFilePath);
      log.info("Creating or replacing existing table view.");
      writer.append(QueryBuilder.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(QueryBuilder.dropSchemaQuery(config.getUser()));
      writer.append(QueryBuilder.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(QueryBuilder.createSchemaQuery(config.getUser()));
      //TODO remove these when possible
      tempQueriesList.add(QueryBuilder.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
      tempQueriesList.add(QueryBuilder.dropSchemaQuery(config.getUser()));
      tempQueriesList.add(QueryBuilder.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
      tempQueriesList.add(QueryBuilder.createSchemaQuery(config.getUser()));

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
        redshiftQueryBuilder.buildFunctionsAndQueryForView(rule, writer, config, url,
            tempQueriesList);
      }
      // TODO add required permission to grant access to different users
      writer.flush();
    } catch (IOException | ClassNotFoundException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while writing to file {}", tempFilePath.getName()), ex);
    }
  }

  @Override
  public void executeSqlQueryForMasking() throws IOException, ClassNotFoundException {
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
      //Dynamically load driver at runtime.
      Class.forName("com.amazon.redshift.jdbc.Driver");
      try (Connection CONN = DriverManager.getConnection(url,
          config.getSuperUser(), config.getSuperUserPassword());
           Statement STMT = CONN.createStatement()) {
        int i =  0;
        for (String query : tempQueriesList) {
          log.info(i + ":" + query);
          STMT.execute(query);
        }
      } catch (SQLException ex) {
        throw new RedmaskRuntimeException(
            String.format("DB Connection error while executing masking sql "
                    + "query from file: {} using super username: {}",
                tempFilePath, config.getSuperUser()), ex);
      }
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
