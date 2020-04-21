package com.hashedin.redmask.integration;

import com.hashedin.redmask.BasePostgresTestContainer;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.service.MaskingService;
import freemarker.template.TemplateException;
import org.apache.ibatis.jdbc.ScriptRunner;
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

import static com.hashedin.redmask.integration.RedMaskITUtils.getMaskConfigurationVersionOne;

public class RedMaskIT extends BasePostgresTestContainer {

  /**
   * Steps for IT tests.
   * 1. Start docker test Container.
   * 2. Create needed DB in test DB.
   * 3. Create table and load data into table.
   * 4. Invoke redmask app with a masking config.
   * 5. Verify if masked view is created.
   *
   * Using developer user, not superuser.
   * 6. Verify for masked data in respective column.
   */


  /**
   * Creating table test_table and populating it
   */
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
  public void sampltetest1() {
    try {
      MaskConfiguration config = getMaskConfigurationVersionOne();
      config.setHost(postgres.getContainerIpAddress());
      config.setPort(String.valueOf(postgres.getMappedPort(5432)));
      config.setDatabase(postgres.getDatabaseName());
      config.setUser("test");
      MaskingService service = new MaskingService(config, false);
      service.generateSqlQueryForMasking();
      service.executeSqlQueryForMasking();
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select age from test.test_table");
      int rowCount = 0;
      while(rs.next()){
        rowCount++;
        Assert.assertTrue(rs.getInt(1)<=10);
        Assert.assertTrue(rs.getInt(1)>0);
      }
      Assert.assertEquals(6,rowCount);
      log.info("completed test one");
    } catch (IOException e) {
      log.error("File not found{}",e);
    } catch (SQLException e) {
      log.error("Sql query error {}",e);
    } catch (TemplateException e) {
      log.error("Template not found{}",e);
    } catch (InstantiationException e) {
      log.error("Instance cannot be created  {}",e);
    } catch (IllegalAccessException e) {
      log.error("Trying to create an object with non public constructor{}",e);
    }
  }

}
