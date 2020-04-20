package com.hashedin.redmask.service;

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
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hashedin.redmask.configurations.MaskingRule;

import freemarker.template.TemplateException;
import com.hashedin.redmask.configurations.MaskConfiguration;

public class MaskingService {

  private static final Logger log = LogManager.getLogger(MaskingService.class);
  private static final String MASKING_FUNCTION_SCHEMA = "redmask";

  private final MaskConfiguration config;
  private String url = "jdbc:postgresql://";
  private final boolean dryRunEnabled;

  // This temp would contain queries to create masked data.
  private final File tempFilePath;

  public MaskingService(MaskConfiguration config, boolean dryRunEnabled)
      throws IOException {
    this.config = config;
    this.url = url + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = createMaskingSqlFile();
  }

  /**
   * Steps: 
   * 
   * Create a masking.sql file. This will contain all required masking queries. 
   * Create Schema.
   * Create masking function for given masking rule.
   * Create View using those masking function.
   * Provide access to user to read data from masked view.
   * 
   * TODO :proper error handling.
   * @throws IOException 
   * @throws SQLException 
   * @throws TemplateException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  public void generateSqlQueryForMasking() throws IOException,
  SQLException, TemplateException, InstantiationException, IllegalAccessException {

    QueryBuilderService queryBuilder = new QueryBuilderService();
    FileWriter writer = new FileWriter(tempFilePath);

    /*
     * Drop and Create redmask Schema and user schema.
     * TODO :find a better way without dropping schema.
     */
    writer.append(queryBuilder.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(queryBuilder.dropSchemaQuery(config.getUser()));
    writer.append(queryBuilder.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(queryBuilder.createSchemaQuery(config.getUser()));

    /**
     * For each masking rule, create postgres mask function.
     * Create view for given table.
     * 
     * First generate query for creating function query for all the masking rule needed.
     * Then we can generate query for creating masked view 
     */
    // Generate query for each table and append in the writer.
    for (int i = 0; i < config.getRules().size(); i++) {
      MaskingRule rule = config.getRules().get(i);

      queryBuilder.buildFunctionsAndQueryForView(rule, writer, config, url);
    }

    // Grant access of this masked view to user.
    writer.append("\n\n-- Grant access to current user on schema: " + MASKING_FUNCTION_SCHEMA + ".\n");
    writer.append("GRANT USAGE ON SCHEMA " + MASKING_FUNCTION_SCHEMA + " TO " + config.getUser() + ";");

    writer.append("\n\n-- Grant access to current user on schema: " + config.getUser() + ".\n");
    writer.append("GRANT ALL PRIVILEGES ON ALL TABLES IN "
        + "SCHEMA " + config.getUser() + " TO " + config.getUser() + ";");

    writer.flush();
    writer.close();
  }

  public void executeSqlQueryForMasking() throws SQLException, FileNotFoundException {
    if (!dryRunEnabled) {
      try (Connection CONN = DriverManager.getConnection(url, 
          config.getSuperUser(), config.getSuperUserPassword())) {
        //Initialize the script runner
        ScriptRunner sr = new ScriptRunner(CONN);

        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader(tempFilePath));

        //Running the script
        sr.setSendFullScript(true);
        sr.runScript(reader);
      }
    }
  }

  private File createMaskingSqlFile() throws IOException {
    // create a temp .sql file
    File sqlFile = File.createTempFile("redmask-masking", ".sql");
    log.info("Created a temp file at location: {}", sqlFile.getAbsolutePath());
    return sqlFile;
  }

}
