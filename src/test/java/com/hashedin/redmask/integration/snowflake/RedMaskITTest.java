package com.hashedin.redmask.integration.snowflake;

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
import org.junit.Ignore;
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
import java.util.Properties;

import static com.hashedin.redmask.integration.snowflake.SnowflakeITUtils.createMaskingRuleVersionFive;
import static com.hashedin.redmask.integration.snowflake.SnowflakeITUtils.createMaskingRuleVersionFour;
import static com.hashedin.redmask.integration.snowflake.SnowflakeITUtils.createMaskingRuleVersionOne;
import static com.hashedin.redmask.integration.snowflake.SnowflakeITUtils.createMaskingRuleVersionSix;
import static com.hashedin.redmask.integration.snowflake.SnowflakeITUtils.createMaskingRuleVersionThree;
import static com.hashedin.redmask.integration.snowflake.SnowflakeITUtils.createMaskingRuleVersionTwo;

/**
 * To run the integration test remove @Ignore annotation and
 * update Snowflake credentials.
 */
@Ignore
public class RedMaskITTest {

  private static final Logger log = LoggerFactory.getLogger(RedMaskITTest.class);
  private static final int ORIGINAL_TABLE_1_ROW_COUNT = 6;
  private static final int ORIGINAL_TABLE_2_ROW_COUNT = 3;
  private static final String TABLE_NAME = "CUSTOMER";
  private static final String TABLE_NAME_2 = "CASHIER";

  private static final String TEST_DATA_FILE = "src/test/resources/HelperSQL/InitializeDB.sql";
  // File to add more data in the tables.
  private static final String INSERT_DATA_FILE = "src/test/resources/HelperSQL/InsertDB.sql";
  private static final String UPDATE_DATA_FILE = "src/test/resources/HelperSQL/UpdateDB.sql";
  private static final String DELETE_DATA_FILE = "src/test/resources/HelperSQL/DeleteDB.sql";

  /**
   * TODO To run the integration test you will have to update below credentials.
   */
  private static final String HOST = "<snowflake-host-url>";
  private static final String PORT = "<snowflake_port>";
  private static final String DATABASE = "<database-name>";
  private static final String SUPER_USER = "<super-user-name>";
  private static final String SUPER_USER_PASSWORD = "<super-user-password>";
  private static final String DEV_USER = "<dev-user-name>";
  private static final String DEV_USER_PASSWORD = "<dev-user-password>";
  private static final String SUPER_USER_SCHEMA = "PUBLIC";
  private static final String DEV_USER_SCHEMA = "<dev-user-schema>";
  private static final String DEV_USER_ROLE = "<dev-user-role>";
  private static final String URL = "jdbc:snowflake://" + HOST;
  private static final String SNOWFLAKE_JDBC_DRIVER = "net.snowflake.client.jdbc.SnowflakeDriver";

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
        DataBaseType.SNOWFLAKE,
        DEV_USER);
  }

  @Before
  public void createConnections() throws SQLException, IOException, ClassNotFoundException {
    log.info("Creating connection object");
    // Create a connection object using super user.
    Class.forName(SNOWFLAKE_JDBC_DRIVER);
    Properties superUserConnProps = new Properties();
    superUserConnProps.setProperty("user", SUPER_USER);
    superUserConnProps.setProperty("password", SUPER_USER_PASSWORD);
    superUserConnProps.setProperty("db", DATABASE);
    superUserConnProps.setProperty("schema", SUPER_USER_SCHEMA);
    connection = DriverManager.getConnection(URL, superUserConnProps);

    // Populate test data in table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(TEST_DATA_FILE));
    sr.setSendFullScript(false);
    sr.runScript(reader);
    log.info("Inserted test data into database.");
    reader.close();

    Properties devUserConnProps = new Properties();
    devUserConnProps.setProperty("user", DEV_USER);
    devUserConnProps.setProperty("password", DEV_USER_PASSWORD);
    devUserConnProps.setProperty("db", DATABASE);
    devUserConnProps.setProperty("schema", DEV_USER_SCHEMA);
    devUserConnProps.setProperty("role", DEV_USER_ROLE);
    devUserConnProps.setProperty("warehouse", "COMPUTE_WH");
    // Create a connection object using developer user.
    devConnection = DriverManager.getConnection(URL, devUserConnProps);
  }

  @After
  public void deleteTableAndMaskedView() throws SQLException {
    Properties superUserConnProps = new Properties();
    superUserConnProps.setProperty("user", SUPER_USER);
    superUserConnProps.setProperty("password", SUPER_USER_PASSWORD);
    superUserConnProps.setProperty("db", DATABASE);
    superUserConnProps.setProperty("schema", SUPER_USER_SCHEMA);
    try (Connection CONN = DriverManager.getConnection(URL, superUserConnProps)) {
      log.info("Dropping tables: {} {}", TABLE_NAME, TABLE_NAME_2);
      Statement stmt = CONN.createStatement();
      stmt.executeUpdate("DROP TABLE " + TABLE_NAME + " CASCADE");
      stmt.executeUpdate("DROP TABLE " + TABLE_NAME_2 + " CASCADE");
      log.info("Dropping existing masked view and closing connection");
      stmt.executeUpdate("DROP VIEW IF EXISTS " + DEV_USER.toUpperCase() + "." + TABLE_NAME);
      stmt.executeUpdate("DROP VIEW IF EXISTS " + DEV_USER.toUpperCase() + "." + TABLE_NAME_2);
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
      Assert.assertTrue(rs.getInt("AGE") <= 10);
      Assert.assertTrue(rs.getInt("AGE") > 0);
      Assert.assertEquals(3.5, rs.getFloat("INTEREST"), 0.01);
      Assert.assertTrue(rs.getString("NAME").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("EMAIL").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("CARD").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));
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
      Assert.assertTrue(rs.getInt("AGE") <= 10);
      Assert.assertTrue(rs.getInt("AGE") > 0);
      Assert.assertEquals(3.5, rs.getFloat("INTEREST"), 0.01);
      Assert.assertTrue(rs.getString("NAME").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("EMAIL").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("CARD").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(6, rowCount);
    // Deletes User Alpha and User Delta from table customer
    deleteDataFromTable();
    int deletedRows = 2;
    rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("AGE") <= 10);
      Assert.assertTrue(rs.getInt("AGE") > 0);
      Assert.assertEquals(3.5, rs.getFloat("INTEREST"), 0.01);
      Assert.assertTrue(rs.getString("NAME").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("EMAIL").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("CARD").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

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
    ResultSet rs = statement.executeQuery("SELECT * FROM " + DEV_USER + "." + TABLE_NAME);
    int rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("AGE") <= 10);
      Assert.assertTrue(rs.getInt("AGE") > 0);
      Assert.assertEquals(3.5, rs.getFloat("INTEREST"), 0.01);
      Assert.assertTrue(rs.getString("NAME").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("EMAIL").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("CARD").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(6, rowCount);
    //Updates age of User Alpha to 14
    String dataChangedUsername = "User Alpha";
    updateDataInTable();
    rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      if (rs.getString("NAME").equals(dataChangedUsername)) {
        Assert.assertEquals(14, rs.getInt("AGE"));
      } else {
        Assert.assertTrue(rs.getInt("AGE") <= 10);
        Assert.assertTrue(rs.getInt("AGE") > 0);
      }
      Assert.assertEquals(3.5, rs.getFloat("INTEREST"), 0.01);
      Assert.assertTrue(rs.getString("NAME").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("EMAIL").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("CARD").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

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
    ResultSet rs = statement.executeQuery("SELECT * FROM " + DEV_USER.toUpperCase() + '.' + TABLE_NAME);
    int rowCount = 0;
    while (rs.next()) {
      rowCount++;
      Assert.assertTrue(rs.getInt("AGE") <= 10);
      Assert.assertTrue(rs.getInt("AGE") > 0);
      Assert.assertEquals(3.5, rs.getFloat("INTEREST"), 0.01);
      Assert.assertTrue(rs.getString("NAME").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("EMAIL").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("CARD").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

    }
    Assert.assertEquals(6, rowCount);
    //Insert addition 6 records into the table
    addMoreDataToTable();
    int insertedRows = 6;
    rs = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
    rowCount = 0;
    while (rs.next()) {
      rowCount += 1;
      Assert.assertTrue(rs.getInt("AGE") <= 10);
      Assert.assertTrue(rs.getInt("AGE") > 0);
      Assert.assertEquals(3.5, rs.getFloat("INTEREST"), 0.01);
      Assert.assertTrue(rs.getString("NAME").matches("^.\\**.$"));
      Assert.assertTrue(rs.getString("EMAIL").matches("^.\\**@.*\\..*"));
      Assert.assertTrue(rs.getString("CARD").matches("^[0-9]{3}x-x{4}-x{3}[0-9]-[0-9]{4}$"));

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
      Assert.assertTrue(rs.getInt("AGE") <= 10);
      Assert.assertTrue(rs.getInt("AGE") > 0);
    }
    Assert.assertEquals(ORIGINAL_TABLE_1_ROW_COUNT, rowCount);
    rs.close();
    statement.close();

    Statement statement2 = devConnection.createStatement();
    ResultSet rs2 = statement2.executeQuery("SELECT * FROM " + TABLE_NAME_2);
    int rowCount2 = 0;
    while (rs2.next()) {
      rowCount2 += 1;
      Assert.assertTrue(rs2.getString("NAME").matches("^.\\**.$"));
    }
    Assert.assertEquals(ORIGINAL_TABLE_2_ROW_COUNT, rowCount2);
    rs2.close();
    statement2.close();
  }

  @Test(expected = RedmaskConfigException.class)
  public void testInvalidTableName() {
    config.setRules(createMaskingRuleVersionTwo());
    runRedMaskApp(config);
  }

  @Test(expected = RedmaskConfigException.class)
  public void testInvalidColumnName() throws IOException {
    config.setRules(createMaskingRuleVersionThree());
    runRedMaskApp(config);
  }

  @Test(expected = RedmaskConfigException.class)
  public void testInvalidParameterValue() throws JsonProcessingException {
    config.setRules(createMaskingRuleVersionFour());
    runRedMaskApp(config);
  }

  @Test(expected = RedmaskConfigException.class)
  public void testUnknownParameterSpecified() throws IOException {
    config.setRules(createMaskingRuleVersionFive());
    runRedMaskApp(config);
  }

  private void runRedMaskApp(MaskConfiguration config) {
    DataMasking dataMasking = DataMaskFactory.buildDataMask(config, false, "dynamic");
    dataMasking.generateSqlQueryForMasking();
    dataMasking.executeSqlQueryForMasking();
  }

  private void addMoreDataToTable() throws IOException {
    // Add additional test data in table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(INSERT_DATA_FILE));
    sr.setSendFullScript(false);
    sr.runScript(reader);
    log.info("Added additional test data.");
    reader.close();
  }

  private void deleteDataFromTable() throws IOException {
    // Delete test data from table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(DELETE_DATA_FILE));
    sr.setSendFullScript(false);
    sr.runScript(reader);
    log.info("Deleted few records from database.");
    reader.close();

  }

  private void updateDataInTable() throws IOException {
    // Update data in table.
    ScriptRunner sr = new ScriptRunner(connection);
    Reader reader = new BufferedReader(
        new FileReader(UPDATE_DATA_FILE));
    sr.setSendFullScript(false);
    sr.runScript(reader);
    reader.close();
    log.info("Updated test data into database.");
  }
}
