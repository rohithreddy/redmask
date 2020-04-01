package com.hashedin.redmask.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingRule;
import com.hashedin.redmask.configurations.Configuration;

public class MaskingService {

  static Logger log = LogManager.getLogger(MaskingService.class);
  private static final String MASKING_FUNCTION_SCHEMA = "redmask";
  
  private Configuration config;
  private String url = "jdbc:postgresql://";
  
  public MaskingService(Configuration config) {
    this.config = config;
    this.url = url + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
  }
  
  /**
   * Steps: 
   * 
   * Create Schema.
   * Create masking function for given masking rule.
   * Create View using those masking function.
   * Provide access to user to read data from masked view.
   * 
   * TODO: proper error handling.
   */
  public void createSchemaAndView() {
    dropSchema();
    createSchema();
    List<MaskingRule> rules = config.getRules();
    
    /**
     * For each masking rule, create postgres mask function.
     * Create view for given table.
     */
    for (MaskingRule maskingRule : rules) {
      try(Connection conn = DriverManager.getConnection(url, 
          config.getSuperUser(), config.getSuperUserPassword())) {
        
        try (PreparedStatement pst = 
            conn.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + MASKING_FUNCTION_SCHEMA)) {
          pst.execute();
        }
        
        if (maskingRule.getRule() == MaskType.RANDOM_PHONE) {
          maskPhoneData(maskingRule, conn);
        }
        
        if (maskingRule.getRule() == MaskType.DESTRUCTION) {
          // create function for destruction masking rule.
        }
        
        if (maskingRule.getRule() == MaskType.EMAIL_MASKING) {
          // create function for email masking rule.
        }

      } catch (SQLException e) {
        log.error("Exception while masking: " + maskingRule + e);
      } finally {

      }
 
    }
  }

  private void maskPhoneData(MaskingRule maskingRule, Connection conn) throws SQLException {

    // get all columns of given table.
    String query = "SELECT * FROM " + maskingRule.getTable();
    boolean maskingColumnExists = false;
    String querySubstring = "";

    Statement st = conn.createStatement();
    ResultSetMetaData rs = st.executeQuery(query).getMetaData();
    
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
    st.close();

    if (!maskingColumnExists) {
      log.error("Column with name: " + maskingRule.getColumn() + " does not exist.");
      return;
    }

    // Create random phone number generation function.
    try (PreparedStatement pst = 
        conn.prepareStatement(MaskingFunctionQuery.randomIntegerBetween())) {
      pst.execute();
    }
    try (PreparedStatement pst = 
        conn.prepareStatement(MaskingFunctionQuery.randomPhone())) {
      pst.execute();
    }

    // Create view
    StringBuilder sb = new StringBuilder();
    sb.append(config.getUsername()).append(".").append(maskingRule.getTable());
    
    String createViewQuery = "CREATE VIEW " + sb.toString() + " AS SELECT " +
        querySubstring +  " FROM " + maskingRule.getTable();

    try (PreparedStatement pst = conn.prepareStatement(createViewQuery)) {
      pst.execute();
    }
    
    //TODO: Grant access of this masked view to user.
  }
  
  // Create a Schema for a given user.
  private void createSchema() {
    String createSchemaQuery = "CREATE SCHEMA IF NOT EXISTS " + config.getUsername();
    try(Connection conn = DriverManager.getConnection(url, 
        config.getSuperUser(), config.getSuperUserPassword());
        PreparedStatement pst = conn.prepareStatement(createSchemaQuery)) {
      pst.execute();
    } catch (SQLException e) {
      log.error(e);
    } finally {

    }
  }
  
  private void dropSchema() {
    String dropSchemaQuery = "drop schema IF EXISTS " + 
        config.getUsername() + ", " + MASKING_FUNCTION_SCHEMA + " CASCADE";
    try(Connection conn = DriverManager.getConnection(url, 
        config.getSuperUser(), config.getSuperUserPassword());
        PreparedStatement pst = conn.prepareStatement(dropSchemaQuery)) {
      pst.execute();
    } catch (SQLException e) {
      log.error(e);
    } finally {

    }
  }

  private void createUserAndGrantAccess() {
    String createUserquery = "CREATE USER developer WITH PASSWORD 'password' VALID UNTIL 'infinity' ";
    String grantAccessquery = "GRANT SELECT ON ALL TABLES IN SCHEMA developer TO developer";
    try(Connection conn = DriverManager.getConnection(url, 
        config.getSuperUser(), config.getSuperUserPassword())) {
      try (PreparedStatement pst = conn.prepareStatement(createUserquery)){
        pst.executeUpdate();
      }

      try (PreparedStatement pst = conn.prepareStatement(grantAccessquery)){
        pst.executeUpdate();
      }

    } catch (SQLException ex) {
      log.error(ex);
    } finally {
    }
  }

}
