package com.hashedin.redmask.integration.redshift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hashedin.redmask.common.DataMasking;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.factory.DataBaseType;
import com.hashedin.redmask.factory.DataMaskFactory;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.hashedin.redmask.integration.redshift.RedMaskITUtils.createMaskingRuleVersionFive;
import static com.hashedin.redmask.integration.redshift.RedMaskITUtils.createMaskingRuleVersionFour;
import static com.hashedin.redmask.integration.redshift.RedMaskITUtils.createMaskingRuleVersionOne;
import static com.hashedin.redmask.integration.redshift.RedMaskITUtils.createMaskingRuleVersionSix;
import static com.hashedin.redmask.integration.redshift.RedMaskITUtils.createMaskingRuleVersionThree;
import static com.hashedin.redmask.integration.redshift.RedMaskITUtils.createMaskingRuleVersionTwo;

/**
 * To run the integration test remove @Ignore annotation and
 * update Redshift credentials.
 */

public class RedMaskITTest {

  private static final Logger log = LoggerFactory.getLogger(RedMaskITTest.class);
  private static final int ORIGINAL_TABLE_1_ROW_COUNT = 6;
  private static final int ORIGINAL_TABLE_2_ROW_COUNT = 3;
  private static final String TABLE_NAME = "customer";
  private static final String TABLE_NAME_2 = "cashier";

  private static final String TEST_DATA_FILE = "src/test/resources/HelperSQL/InitializeDB.sql";
  // File to add more data in the tables.
  private static final String INSERT_DATA_FILE = "src/test/resources/HelperSQL/InsertDB.sql";
  private static final String UPDATE_DATA_FILE = "src/test/resources/HelperSQL/UpdateDB.sql";
  private static final String DELETE_DATA_FILE = "src/test/resources/HelperSQL/DeleteDB.sql";

  /**
   * TODO To run the integration test you will have to update below credentials.
   */
  private static final String HOST = "<redshift-host-url>";
  private static final String PORT = "5439";
  private static final String DATABASE = "<database-name>";
  private static final String SUPER_USER = "<super-user-name>";
  private static final String SUPER_USER_PASSWORD = "<super-user-password>";
  private static final String DEV_USER = "<dev-user-name>";
  private static final String DEV_USER_PASSWORD = "<dev-user-password>";
  private static final String URL = "jdbc:redshift://" + HOST + ":" + PORT + "/" + DATABASE;

  private static MaskConfiguration config = null;
  private Connection devConnection;
  private static Connection connection = null;

  /**
   * TODO :Verify for masked data in respective column for string and card type.
   * TODO :add negative test cases with exception
   * TODO :replace the below test with test on adding additional data, 
   *       delete update of data on multiple tables and columns
   */

  /**
   * Creating table test_table and populating it
   *
   * @throws SQLException
   * @throws IOException
   */
  @BeforeClass
  public static void setup() throws SQLException, IOException {
      log.info("Setting up integration test configuration.");
      // Define Masking config.
      config = new MaskConfiguration(SUPER_USER,
          SUPER_USER_PASSWORD,
          DEV_USER,
          DEV_USER_PASSWORD,
          HOST,
          PORT,
          DATABASE,
          DataBaseType.REDSHIFT,
          DEV_USER);
  }

  @Before
  public void createConnections() throws SQLException, IOException, ClassNotFoundException {
    log.info("Creating connection object");
    // Create a connection object using super user.
    Class.forName("com.amazon.redshift.jdbc.Driver");
    connection = DriverManager.getConnection(
        URL,
        SUPER_USER,
        SUPER_USER_PASSWORD
    );

    // Populate test data in table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(TEST_DATA_FILE));
    sr.setSendFullScript(true);
    sr.runScript(reader);
    log.info("Inserted test data into database.");
    reader.close();

    // Create a connection object using developer user.
    devConnection = DriverManager.getConnection(
        URL,
        DEV_USER,
        DEV_USER_PASSWORD
    );
  }

  @After
  public void deleteTableAndMaskedView() throws SQLException {
    try (Connection CONN = DriverManager.getConnection(
        URL,
        SUPER_USER,
        SUPER_USER_PASSWORD
    )) {
      log.info("Dropping tables: {} {}", TABLE_NAME, TABLE_NAME_2);
      Statement stmt = CONN.createStatement();
      stmt.executeUpdate("DROP TABLE " + TABLE_NAME + "," + TABLE_NAME_2 + " CASCADE");
      log.info("Dropping existing masked view and closing connection");
      stmt.executeUpdate("DROP VIEW IF EXISTS " + DEV_USER + "." + TABLE_NAME
          + "," + DEV_USER + "." + TABLE_NAME_2);
      stmt.close();
    }
    connection.close();
    devConnection.close();
  }

  @Test
  public void testMultipleMaskSingleTable() throws IOException, SQLException,
      ClassNotFoundException {
    config.setRules(createMaskingRuleVersionOne());
    runRedMaskApp(config);
    Statement statement = devConnection.createStatement();
    ResultSet rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    int rowCount = 0;
    while (rs.next()) {
      rowCount++;
      Assert.assertTrue(rs.getInt("age") <= 10);
      Assert.assertTrue(rs.getInt("age") > 0);
      Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      Assert.assertTrue(rs.getString("name").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("email").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("card").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));
    }
    Assert.assertEquals(ORIGINAL_TABLE_1_ROW_COUNT, rowCount);
    rs.close();
    statement.close();
  }

  @Test
  public void testMultipleMaskSingleTableDeleteData() throws IOException, SQLException,
      ClassNotFoundException {
    config.setRules(createMaskingRuleVersionOne());
    runRedMaskApp(config);
    Statement statement = devConnection.createStatement();
    ResultSet rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    int rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("age") <= 10);
      Assert.assertTrue(rs.getInt("age") > 0);
      Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      Assert.assertTrue(rs.getString("name").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("email").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("card").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(6, rowCount);
    // Deletes User Alpha and User Delta from table customer
    deleteDataFromTable();
    int deletedRows = 2;
    rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("age") <= 10);
      Assert.assertTrue(rs.getInt("age") > 0);
      Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      Assert.assertTrue(rs.getString("name").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("email").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("card").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(ORIGINAL_TABLE_1_ROW_COUNT - deletedRows, rowCount);
    rs.close();
    statement.close();
  }

  @Test
  public void testMultipleMaskSingleTableUpdateData() throws IOException, SQLException,
      ClassNotFoundException {
    config.setRules(createMaskingRuleVersionOne());
    runRedMaskApp(config);
    Statement statement = devConnection.createStatement();
    ResultSet rs = statement.executeQuery("SELECT * FROM " + DEV_USER+"."+TABLE_NAME);
    int rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("age") <= 10);
      Assert.assertTrue(rs.getInt("age") > 0);
      Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      Assert.assertTrue(rs.getString("name").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("email").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("card").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(6, rowCount);
    //Updates age of User Alpha to 14
    String dataChangedUsername = "User Alpha";
    updateDataInTable();
    rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      if (rs.getString("name").equals(dataChangedUsername)) {
        Assert.assertEquals(14, rs.getInt("age"));
      } else {
        Assert.assertTrue(rs.getInt("age") <= 10);
        Assert.assertTrue(rs.getInt("age") > 0);
      }
      Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      Assert.assertTrue(rs.getString("name").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("email").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("card").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(ORIGINAL_TABLE_1_ROW_COUNT, rowCount);
    rs.close();
    statement.close();
  }

  @Test
  public void testMultipleMaskSingleTableInsertData() throws IOException, SQLException,
      ClassNotFoundException {
    config.setRules(createMaskingRuleVersionOne());
    runRedMaskApp(config);
    Statement statement = devConnection.createStatement();
    ResultSet rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    int rowCount = 0;
    while (rs.next()) {
      rowCount++;
      Assert.assertTrue(rs.getInt("age") <= 10);
      Assert.assertTrue(rs.getInt("age") > 0);
      Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      Assert.assertTrue(rs.getString("name").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("email").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("card").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(6, rowCount);
    //Insert addition 6 records into the table
    addMoreDataToTable();
    int insertedRows = 6;
    rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("age") <= 10);
      Assert.assertTrue(rs.getInt("age") > 0);
      Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      Assert.assertTrue(rs.getString("name").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("email").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("card").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(ORIGINAL_TABLE_1_ROW_COUNT + insertedRows, rowCount);
    rs.close();
    statement.close();
  }

  @Test
  public void testMultipleTables() throws IOException, SQLException, ClassNotFoundException {
    config.setRules(createMaskingRuleVersionSix());
    runRedMaskApp(config);
    Statement statement = devConnection.createStatement();
    ResultSet rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    int rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("age") <= 10);
      Assert.assertTrue(rs.getInt("age") > 0);
    }
    Assert.assertEquals(ORIGINAL_TABLE_1_ROW_COUNT, rowCount);
    rs.close();
    statement.close();

    Statement statement2 = devConnection.createStatement();
    ResultSet rs2 = statement2.executeQuery("SELECT * FROM " + TABLE_NAME_2);
    int rowCount2 = 0;
    while (rs2.next()) {
      rowCount2 += 1;
      Assert.assertTrue(rs2.getString("name").matches("^.\\**.$"));
    }
    Assert.assertEquals(ORIGINAL_TABLE_2_ROW_COUNT, rowCount2);
    rs2.close();
    statement2.close();
  }

  @Test(expected = RedmaskConfigException.class)
  public void testInvalidTableName() {
    config.setRules(createMaskingRuleVersionTwo());
    try {
      runRedMaskApp(config);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Test(expected = RedmaskConfigException.class)
  public void testInvalidColumnName() throws IOException {
    config.setRules(createMaskingRuleVersionThree());
    try {
      runRedMaskApp(config);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Test(expected = RedmaskConfigException.class)
  public void testInvalidParameterValue() throws JsonProcessingException {
    config.setRules(createMaskingRuleVersionFour());
    try {
      runRedMaskApp(config);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Test(expected = RedmaskConfigException.class)
  public void testUnknownParameterSpecified() throws IOException {
    config.setRules(createMaskingRuleVersionFive());
    try {
      runRedMaskApp(config);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void runRedMaskApp(MaskConfiguration config) throws IOException, ClassNotFoundException {
    DataMasking dataMasking = DataMaskFactory.buildDataMask(config, false);
    dataMasking.generateSqlQueryForMasking();
    dataMasking.executeSqlQueryForMasking();
  }

  private void addMoreDataToTable() throws SQLException, IOException {
    // Add additional test data in table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(INSERT_DATA_FILE));
    sr.setSendFullScript(true);
    sr.runScript(reader);
    log.info("Added additional test data.");
    reader.close();
  }

  private void deleteDataFromTable() throws SQLException, IOException {
    // Delete test data from table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(DELETE_DATA_FILE));
    sr.setSendFullScript(true);
    sr.runScript(reader);
    log.info("Deleted few records from database.");
    reader.close();

  }

  private void updateDataInTable() throws SQLException, IOException {
    // Update data in table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(UPDATE_DATA_FILE));
    sr.setSendFullScript(true);
    sr.runScript(reader);
    reader.close();
    log.info("Updated test data into database.");
  }
}
