package com.hashedin.redmask.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingRule;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

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
    // create a .sql file
    File sqlFile = new File(MASKING_SQL_FILE_PATH);
    if (!sqlFile.createNewFile()) {
      // delete the existing file and create again.
      sqlFile.delete();
      sqlFile.createNewFile();
    }
    FileWriter writer = new FileWriter(MASKING_SQL_FILE_PATH);

    // TODO: find a better way without dropping schema.
    writer.append(BuildQueryUtil.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(BuildQueryUtil.dropSchemaQuery(config.getUser()));

    // Create Schema
    writer.append(BuildQueryUtil.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(BuildQueryUtil.createSchemaQuery(config.getUser()));

    /**
     * For each masking rule, create postgres mask function.
     * Create view for given table.
     */
    for (MaskingRule maskingRule : config.getRules()) {

      if (maskingRule.getRule() == MaskType.RANDOM_PHONE) {
        maskPhoneData(maskingRule, writer);
      }

      if (maskingRule.getRule() == MaskType.DESTRUCTION) {
        // create function for destruction masking rule.
      }

      if (maskingRule.getRule() == MaskType.EMAIL_MASKING) {
        // create function for email masking rule.
      }
    }
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

  private void maskPhoneData(MaskingRule maskingRule, FileWriter writer) 
      throws SQLException, IOException, TemplateException {

    // get all columns of given table.
    String query = "SELECT * FROM " + maskingRule.getTable();
    boolean maskingColumnExists = false;
    String querySubstring = "";
    ResultSetMetaData rs = null;

    try(Connection conn = DriverManager.getConnection(url, 
        config.getSuperUser(), config.getSuperUserPassword());
        Statement st = conn.createStatement()) {
      rs = st.executeQuery(query).getMetaData();
    }

    // Dynamically build sub query part for create view.
    for (int i = 1; i <= rs.getColumnCount(); i++) {
      if (rs.getColumnName(i).equals(maskingRule.getColumn()) ) {
        querySubstring = querySubstring + ", redmask.random_phone('0') as " + rs.getColumnName(i);
        maskingColumnExists = true;
      } else if(querySubstring.isEmpty()){
        querySubstring = rs.getColumnName(i);
      } else {
        querySubstring = querySubstring + ", " + rs.getColumnName(i);
      }
    }

    if (!maskingColumnExists) {
      log.error("Column with name: " + maskingRule.getColumn() + " does not exist.");
      return;
    }

    String createFunString = processTemplate("random_int_between");

    // Create random phone number generation function.
    writer.append("\n\n-- Postgres function to generate ranadom between given two integer.\n");
    writer.append(MaskingFunctionQuery.randomIntegerBetween(createFunString.toString()));

    String createPhoneFun = processTemplate("random_phone");
    writer.append("\n\n-- Postgres function to generate ranadom phone number data.\n");
    writer.append(MaskingFunctionQuery.randomPhone(createPhoneFun));

    // Create view
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(maskingRule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT " +
        querySubstring +  " FROM " + maskingRule.getTable() + ";";

    writer.append("\n\n-- Create masked view.\n");
    writer.append(createViewQuery);

    //TODO: Grant access of this masked view to user.
  }

  private String processTemplate(String functionName)
      throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
    Map<String, String> input = new HashMap<String, String>();
    input.put("schema", MASKING_FUNCTION_SCHEMA);
    input.put("functionName", functionName);
    Template temp = config.getTemplateConfig().getConfig().getTemplate("create_function.txt");
    StringWriter stringWriter = new StringWriter();
    temp.process(input, stringWriter);
    String createFunString = stringWriter.toString();
    stringWriter.close();
    input.clear();
    return createFunString;
  }

}
