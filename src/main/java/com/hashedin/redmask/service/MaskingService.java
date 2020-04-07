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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingRule;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import com.hashedin.redmask.configurations.ColumnRule;
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
    writer.append(BuildQueryUtil.dropSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(BuildQueryUtil.dropSchemaQuery(config.getUser()));

    // Create Schema
    writer.append(BuildQueryUtil.createSchemaQuery(MASKING_FUNCTION_SCHEMA));
    writer.append(BuildQueryUtil.createSchemaQuery(config.getUser()));

    /**
     * For each masking rule, create postgres mask function.
     * Create view for given table.
     * 
     * First generate query for creating function query for all the masking rule needed.
     * Then we can generate query for creating masked view 
     */
    generateQueryToCreateMaskingFunctions(writer);

    // Generate query for each table and append in the writer.
    for (int i = 0; i < config.getRules().size(); i++ ) {
      MaskingRule rule = config.getRules().get(i);
      buildQueryForView(rule, writer);
    }

    writer.flush();
    writer.close();
  }

  private void generateQueryToCreateMaskingFunctions(FileWriter writer)
      throws TemplateNotFoundException, MalformedTemplateNameException,
      ParseException, IOException, TemplateException {
    Set<MaskType> maskingSet = new HashSet<>();
    for (MaskingRule maskingRule : config.getRules()) {
      for (ColumnRule col: maskingRule.getColumns()) {
        maskingSet.add(col.getRule());
      }
    }
    // create functions for all the required masking rule.
    for(MaskType type: maskingSet) {
      switch(type) {
      case RANDOM_PHONE:
        MaskingFunctionQuery.randomPhone(config, writer);
        break;
      case TEXT_MASKING:
        MaskingFunctionQuery.maskString(config, writer);
        break;
      case EMAIL_MASKING:
        // Do something for email masking
        break;
      case DESTRUCTION:
        // Do something for destruction masking type.
        break;
      }
    }
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

  private void buildQueryForView(MaskingRule rule, FileWriter writer) 
      throws SQLException, IOException {

    // get all columns of given table.
    String query = "SELECT * FROM " + rule.getTable();

    // TODO: Use String builder here.
    String querySubstring = "";
    ResultSetMetaData rs = null;

    try(Connection conn = DriverManager.getConnection(url, 
        config.getSuperUser(), config.getSuperUserPassword());
        Statement st = conn.createStatement()) {
      rs = st.executeQuery(query).getMetaData();
    }

    Map<String, MaskType> colMaskRuleMap = new HashMap<>();
    for (ColumnRule col : rule.getColumns()) {
      colMaskRuleMap.put(col.getName(), col.getRule());
    }

    // TODO: Add validation, if column to be masked does not exists.

    // Dynamically build sub query part for create view.
    for (int i = 1; i <= rs.getColumnCount(); i++) {
      String colName = rs.getColumnName(i);
      if (colMaskRuleMap.containsKey(colName)) {
        // TODO: Remove this if conditions and improve logic here.
        if (colMaskRuleMap.get(colName) == MaskType.RANDOM_PHONE)
          querySubstring = querySubstring + ", " + MASKING_FUNCTION_SCHEMA + "." + "random_phone()" +" as " + colName;
        if (colMaskRuleMap.get(colName) == MaskType.TEXT_MASKING) {
          querySubstring = querySubstring + ", " + MASKING_FUNCTION_SCHEMA + "." 
              + "anonymize(" + colName + ") as " + colName;
        }

      } else if(querySubstring.isEmpty()){
        querySubstring = rs.getColumnName(i);
      } else {
        querySubstring = querySubstring + ", " + rs.getColumnName(i);
      }
    }

    // Create view
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(rule.getTable());

    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT " +
        querySubstring +  " FROM " + rule.getTable() + ";";

    writer.append("\n\n-- Create masked view.\n");
    writer.append(createViewQuery);

    //TODO: Grant access of this masked view to user.
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
