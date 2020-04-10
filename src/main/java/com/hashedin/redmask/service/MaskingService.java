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
  private static final String MASKING_SQL_FILE_PATH = "src/main/resources/masking.sql";

  private MaskConfiguration config;
  private String url = "jdbc:postgresql://";
  private boolean dryRunEnabled;

  public MaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    this.config = config;
    this.url = url + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
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
   * TODO: proper error handling.
   * @throws IOException 
   * @throws SQLException 
   * @throws TemplateException 
   */
  public void generateSqlQueryForMasking() throws IOException, SQLException, TemplateException {
    //create a .sql file which would contain queries to create masked data.
    FileWriter writer = createMaskingSqlFile();

    // TODO: find a better way without dropping schema.
    writer.append(QueryBuilderUtil.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(QueryBuilderUtil.dropSchemaQuery(config.getUser()));

    // Create Schema
    writer.append(QueryBuilderUtil.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(QueryBuilderUtil.createSchemaQuery(config.getUser()));

    /**
     * For each masking rule, create postgres mask function.
     * Create view for given table.
     * 
     * First generate query for creating function query for all the masking rule needed.
     * Then we can generate query for creating masked view 
     */
    QueryBuilderUtil.generateQueryToCreateMaskingFunctions(writer, config);

    // Generate query for each table and append in the writer.
    for (int i = 0; i < config.getRules().size(); i++ ) {
      MaskingRule rule = config.getRules().get(i);
      QueryBuilderUtil.buildQueryForView(rule, writer, config, url);
    }
    
    // Grant access of this masked view to user.
    writer.append("\n\n-- Grant access to current user on schema: " + MASKING_FUNCTION_SCHEMA + ".\n");
    writer.append("GRANT USAGE ON SCHEMA "+ MASKING_FUNCTION_SCHEMA + " TO " + config.getUser() + ";");
    
    writer.append("\n\n-- Grant access to current user on schema: " + config.getUser() + ".\n");
    writer.append("GRANT ALL PRIVILEGES ON ALL TABLES IN "
        + "SCHEMA " + config.getUser() + " TO " + config.getUser() + ";");

    writer.flush();
    writer.close();
  }

  public void executeSqlQueryForMasking() throws SQLException, FileNotFoundException {
    if (!dryRunEnabled) {
      try(Connection conn = DriverManager.getConnection(url, 
          config.getSuperUser(), config.getSuperUserPassword())) {
        //Initialize the script runner
        ScriptRunner sr = new ScriptRunner(conn);

        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader(MASKING_SQL_FILE_PATH));

        //Running the script
        sr.setSendFullScript(true);
        sr.runScript(reader);
      }
    }
  }

  private FileWriter createMaskingSqlFile() throws IOException {
    // create a .sql file
    File sqlFile = new File(MASKING_SQL_FILE_PATH);
    if (!sqlFile.createNewFile()) {
      // delete the existing file and create again.
      sqlFile.delete();
      sqlFile.createNewFile();
    }
    return new FileWriter(MASKING_SQL_FILE_PATH);
  }

}
