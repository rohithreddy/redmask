package com.hashedin.redmask.postgres;

import com.hashedin.redmask.common.QueryBuilder;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.config.MaskingRule;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import com.hashedin.redmask.factory.DataMasking;

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
    this.tempFilePath = createMaskingSqlFile();
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
  public void generateSqlQueryForMasking() {

    PostgresQueryBuilder postgresQueryBuilder = new PostgresQueryBuilder();
    try {
      FileWriter writer = new FileWriter(tempFilePath);

      /*
       * Drop and Create redmask Schema and user schema.
       * TODO :Find a better way without dropping schema.
       */
      log.info("Creating or replacing existing table view.");
      writer.append(QueryBuilder.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(QueryBuilder.dropSchemaQuery(config.getUser()));
      writer.append(QueryBuilder.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
      writer.append(QueryBuilder.createSchemaQuery(config.getUser()));

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
        postgresQueryBuilder.buildFunctionsAndQueryForView(rule, writer, config, url);
      }

      // Grant access of this masked view to user.
      log.info("Required permission have been granted to the specified user.");
      writer.append("\n\n-- Grant access to current user on schema: "
          + MASKING_FUNCTION_SCHEMA + ".\n");
      writer.append("GRANT USAGE ON SCHEMA " + MASKING_FUNCTION_SCHEMA
          + " TO " + config.getUser() + ";");
      writer.append("\n\n-- Grant access to current user on schema: " + config.getUser() + ".\n");
      writer.append("GRANT ALL PRIVILEGES ON ALL TABLES IN "
          + "SCHEMA " + config.getUser() + " TO " + config.getUser() + ";");
      writer.append("\nGRANT USAGE ON SCHEMA " + config.getUser()
          + " TO " + config.getUser() + ";");

      writer.flush();
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          String.format("Error while writing to file {}", tempFilePath.getName()), ex);
    }
  }

  public void executeSqlQueryForMasking() throws IOException {
    if (!dryRunEnabled) {
      log.info("Executing script in order to create view in the database.");
      Reader reader = null;
      try (Connection CONN = DriverManager.getConnection(url,
          config.getSuperUser(), config.getSuperUserPassword())) {
        //Initialize the script runner
        ScriptRunner sr = new ScriptRunner(CONN);

        //Creating a reader object
        reader = new BufferedReader(new FileReader(tempFilePath));

        //Running the script
        sr.setSendFullScript(true);
        sr.runScript(reader);
      } catch (SQLException ex) {
        throw new RedmaskRuntimeException(
            String.format("DB Connection error while executing masking sql "
                + "query from file: {} using super username: {}",
                tempFilePath, config.getSuperUser()), ex);
      } catch (FileNotFoundException ex) {
        throw new RedmaskRuntimeException(
            String.format("Masking sql query file {} not found", tempFilePath.getName()), ex);
      } finally {
        if (reader != null) {
          reader.close();
        }
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
