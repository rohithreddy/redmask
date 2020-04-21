package com.hashedin.redmask.integration;

import com.hashedin.redmask.BasePostgresTestContainer;
import com.hashedin.redmask.IntegerFloatMaskingFunctionTest;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.service.MaskingService;
import freemarker.template.TemplateException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

public class RedMaskIT extends BasePostgresTestContainer {

  private static final Logger log = LogManager.getLogger(IntegerFloatMaskingFunctionTest.class);

  private static final String SUPERUSER = "test";
  private static final String SUPERUSER_PASSWORD = "test";
  private static final String USER_NAME = "test";
  private static final String USER_PASSWORD = "test";
  private static final String HOST = postgres.getContainerIpAddress();
  private static final String PORT = String.valueOf(postgres.getMappedPort(5432));
  private static final String DATABASE = postgres.getDatabaseName();
  private static final String USER = "test";

  private static MaskConfiguration config = null;


  /**
   * TODO  Verify if masked view is created.
   * TODO Using developer user, not superuser.
   * TODO Verify for masked data in respective column for string and card type.
   * TODO add negative test cases
   */


  /**
   * Creating table test_table and populating it
   */

  @BeforeClass
  public static void createMaskConfig() {
    try {
      config = new MaskConfiguration(SUPERUSER,
          SUPERUSER_PASSWORD,
          USER_NAME,
          USER_PASSWORD,
          HOST,
          PORT,
          DATABASE,
          USER);
    } catch (IOException e) {
      log.error("error creating default masking configuration", e);
    }
  }

  @BeforeClass
  public static void populateTestTable() {
    ScriptRunner sr = new ScriptRunner(connection);
    //Creating a reader object
    Reader reader = null;
    try {
      reader = new BufferedReader(
          new FileReader("src/test/java/com/hashedin/redmask/integration/HelperSQL/populateDB.sql"));
      //Running the script
      sr.setSendFullScript(true);
      sr.runScript(reader);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testIntegerWithinRange() {
    try {
      config.setRules(getMaskRuleIntegerWithinRange());
      runRedMaskTest(config);
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select age from test.test_table");
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertTrue(rs.getInt("age") <= 10);
        Assert.assertTrue(rs.getInt("age") > 0);
      }
      Assert.assertEquals(6, rowCount);
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
      runRedMaskTest(config);
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select age from test.test_table");
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertTrue(rs.getInt("age") < 10000);
        Assert.assertTrue(rs.getInt("age") > 999);
      }
      Assert.assertEquals(6, rowCount);
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
      runRedMaskTest(config);
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select age from test.test_table");
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals(4, rs.getInt("age"));
      }
      Assert.assertEquals(6, rowCount);
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
      runRedMaskTest(config);
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select interest from test.test_table");
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals(3.5, rs.getFloat("interest"), 0.01);
      }
      Assert.assertEquals(6, rowCount);
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
      runRedMaskTest(config);
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select age from test.test_table");
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals("[0,10)", rs.getString(1));
      }
      Assert.assertEquals(6, rowCount);
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
      runRedMaskTest(config);
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select age from test.test_table");
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals("[0,1000)", rs.getString(1));
      }
      Assert.assertEquals(6, rowCount);
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
      runRedMaskTest(config);
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select age from test.test_table");
      int rowCount = 0;
      while (rs.next()) {
        rowCount += 1;
        Assert.assertEquals("[0,100)", rs.getString(1));
      }
      Assert.assertEquals(6, rowCount);
      statement.close();
    } catch (IOException e) {
      log.error("File not found{}", e);
    } catch (SQLException e) {
      log.error("Sql query error {}", e);
    }
  }

  @After
  public void deleteCreatedView() {
    try {
      Statement stmt = connection.createStatement();
      stmt.execute("drop view if exists test.test_table;");
      stmt.close();
    } catch (SQLException ex) {
      log.error("Error while dropping existing view", ex);
    }
  }


  private void runRedMaskTest(MaskConfiguration config) {
    MaskingService service = null;
    try {
      service = new MaskingService(config, false);
      service.generateSqlQueryForMasking();
      service.executeSqlQueryForMasking();
    } catch (IOException | InstantiationException | SQLException | IllegalAccessException | TemplateException ex) {
      log.error("Exception occurred while running the RedMaskApp", ex);
    }
  }
}



