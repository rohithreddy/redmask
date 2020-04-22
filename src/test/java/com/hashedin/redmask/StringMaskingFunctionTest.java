package com.hashedin.redmask;

import com.hashedin.redmask.configurations.MaskingConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StringMaskingFunctionTest extends BasePostgresTestContainer {

  private static final Logger log = LogManager.getLogger(StringMaskingFunctionTest.class);

  private static final String CREATE_FUNCTION = "CREATE OR REPLACE FUNCTION %s.%s";

  @BeforeClass
  public static void addStringMaskingFunction() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA,
          MaskingConstants.MASK_STRING_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_STRING_FILE);

      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
    } catch (SQLException err) {
      log.error("Exception addin gthe String masking function", err);
    } catch (IOException e) {
      log.error("Unable to get file containing SQL function", e);
    }
  }

  @Test
  public void testStringMaskWithDefaultParameters() {
    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      // When only string is given
      String selectquery = "Select " + SCHEMA + ".anonymize('abcdefghij') as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("xxxxxxxxxx", rs.getString(1));
    } catch (SQLException throwables) {
      log.error("Error while executing Sql query",throwables);
    }

  }

  @Test
  public void testStringMaskWithSpecifiedSeparator() {

    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      // When string and masking pattern is given
      String selectquery = "Select " + SCHEMA + ".anonymize('abcdefghij','*') as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("**********", rs.getString(1));
    } catch (SQLException throwables) {
      log.error("Error while executing Sql query",throwables);
    }

  }

  @Test
  public void testStringMaskWithSeparatorandPrefixLength() {
    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      // When string, masking pattern, prefix length is given
      String selectquery = "Select " + SCHEMA + ".anonymize('abcdefghij','#',3) as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("abc#######", rs.getString(1));
    } catch (SQLException throwables) {
      log.error("Error while executing Sql query",throwables);
    }

  }

  @Test
  public void testStringMaskWithSeparatorPrefixSuffixLength() {
    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      // When string, masking pattern, prefix  and suffix length is given
      String selectquery = "Select " + SCHEMA + ".anonymize('abcdefghij','x',3,2) as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("abcxxxxxij", rs.getString(1));

    } catch (SQLException throwables) {
      log.error("Error while executing Sql query",throwables);
    }
  }
}

