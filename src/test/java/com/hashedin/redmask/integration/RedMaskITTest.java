package com.hashedin.redmask.integration;

import com.hashedin.redmask.configurations.ColumnNotFoundException;
import com.hashedin.redmask.configurations.InvalidParameterValueException;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.TableNotFoundException;
import com.hashedin.redmask.configurations.UnknownParameterException;
import com.hashedin.redmask.service.MaskingService;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskRuleBigIntRange;
import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskRuleFloatFixedValue;
import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskRuleIntegerFixedSize;
import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskRuleIntegerFixedValue;
import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskRuleIntegerRange;
import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskRuleIntegerWithinRange;
import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskRuleNumericRange;

public class RedMaskITTest extends BaseITPostgresTestContainer {

  private static final Logger log = LogManager.getLogger(RedMaskITTest.class);

  protected static final String URL = postgres.getJdbcUrl();
  protected static final String HOST = postgres.getContainerIpAddress();
  protected static final String PORT = String.valueOf(postgres.getMappedPort(5432));
  protected static final String DATABASE = postgres.getDatabaseName();
  protected static final String SUPER_USER = postgres.getUsername();
  protected static final String SUPER_USER_PASSWORD = postgres.getPassword();

  private static MaskConfiguration config = null;
  private Connection devConnection;

  /**
   * TODO Verify for masked data in respective column for string and card type.
   * TODO add negative test cases with exception
   * TODO replace the below test with test on adding additional data, delete update of data on multiple tables and columns
   */

  /**
   * Creating table test_table and populating it
   *
   * @throws SQLException
   */
  @BeforeClass
  public static void setup() throws SQLException {
    try {
      // Create a developer user.
      connection = DriverManager.getConnection(URL, SUPER_USER, SUPER_USER_PASSWORD);
      Statement statement = connection.createStatement();
      String createUser = "CREATE USER " + DEV_USER + " WITH PASSWORD '" + DEV_USER_PASSWORD + "'";
      statement.executeUpdate(createUser);
      statement.close();

      // Populate test data in table.
      ScriptRunner sr = new ScriptRunner(connection);
      Reader reader = new BufferedReader(
          new FileReader(TEST_DATA_FILE));
      sr.setSendFullScript(true);
      sr.runScript(reader);
      log.info("Inserted test data into database.");

      // Define Masking config.
      config = new MaskConfiguration(SUPER_USER,
          SUPER_USER_PASSWORD,
          DEV_USER,
          DEV_USER_PASSWORD,
          HOST,
          PORT,
          DATABASE,
          DEV_USER);

    } catch (Exception e) {
      log.error("error setting up integration test configuration", e);
    } finally {
      connection.close();
    }
  }

  @Before
  public void createConnections() throws SQLException {
    try {
      // Create a connection object using super user.
      connection = DriverManager.getConnection(
          URL,
          SUPER_USER,
          SUPER_USER_PASSWORD
      );

      // Create a connection object using developer user.
      devConnection = DriverManager.getConnection(
          URL,
          DEV_USER,
          DEV_USER_PASSWORD
      );
    } catch (SQLException e) {
      log.error("error creating connection object", e);
    }
  }

  @After
  public void deleteCreatedView() {
    try {
      // Drop existing masked view if any.
      Statement stmt = connection.createStatement();
      stmt.execute("DROP VIEW IF EXISTS " + DEV_USER + "." + TABLE_NAME);
      stmt.close();
      connection.close();
      devConnection.close();
    } catch (SQLException ex) {
      log.error("Error while dropping existing view and closing connection", ex);
    }
  }

  @Test
  public void testIntegerWithinRangeMasking() {
    try {
      config.setRules(getMaskRuleIntegerWithinRange());
      runRedMaskApp(config);
      Statement statement = devConnection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT age FROM " + DEV_USER + "." + TABLE_NAME);
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertTrue(rs.getInt("age") <= 10);
        Assert.assertTrue(rs.getInt("age") > 0);
      }
      Assert.assertEquals(6, rowCount);
      rs.close();
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  @Test
  public void testFixedSizeInteger() {
    try {
      config.setRules(getMaskRuleIntegerFixedSize());
      runRedMaskApp(config);
      Statement statement = devConnection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT age FROM " + DEV_USER + "." + TABLE_NAME);
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertTrue(rs.getInt("age") < 10000);
        Assert.assertTrue(rs.getInt("age") > 999);
      }
      Assert.assertEquals(6, rowCount);
      rs.close();
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  @Test
  public void testFixedValueInteger() {
    try {
      config.setRules(getMaskRuleIntegerFixedValue());
      runRedMaskApp(config);
      Statement statement = devConnection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT age FROM " + DEV_USER + "." + TABLE_NAME);
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals(4, rs.getInt("age"));
      }
      Assert.assertEquals(6, rowCount);
      rs.close();
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  @Test
  public void testFixedValueFloat() {
    try {
      config.setRules(getMaskRuleFloatFixedValue());
      runRedMaskApp(config);
      Statement statement = devConnection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT interest FROM " + DEV_USER + "." + TABLE_NAME);
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      }
      Assert.assertEquals(6, rowCount);
      rs.close();
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  @Test
  public void testIntegerRange() {
    try {
      config.setRules(getMaskRuleIntegerRange());
      runRedMaskApp(config);
      Statement statement = devConnection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT age FROM " + DEV_USER + "." + TABLE_NAME);
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals("[0,10)", rs.getString(1));
      }
      Assert.assertEquals(6, rowCount);
      rs.close();
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  @Test
  public void testBigIntegerRange() {
    try {
      config.setRules(getMaskRuleBigIntRange());
      runRedMaskApp(config);
      Statement statement = devConnection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT age FROM " + DEV_USER + "." + TABLE_NAME);
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals("[0,1000)", rs.getString(1));
      }
      Assert.assertEquals(6, rowCount);
      rs.close();
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  @Test
  public void testNumericRange() {
    try {
      config.setRules(getMaskRuleNumericRange());
      runRedMaskApp(config);
      Statement statement = devConnection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT age FROM " + DEV_USER + "." + TABLE_NAME);
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals("[0,100)", rs.getString(1));
      }
      Assert.assertEquals(6, rowCount);
      rs.close();
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  private void runRedMaskApp(MaskConfiguration config) {
    try {
      MaskingService service = new MaskingService(config, false);
      service.generateSqlQueryForMasking();
      service.executeSqlQueryForMasking();
    } catch (UnknownParameterException | InvalidParameterValueException |
        TableNotFoundException | ColumnNotFoundException ex) {
      log.error("Exception occurred while running the RedMaskApp", ex);
    }
  }
}
