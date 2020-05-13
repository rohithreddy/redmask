package com.hashedin.redmask;

import com.hashedin.redmask.config.MaskingConstants;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.hashedin.redmask.config.MaskingConstants.MASK_STRING_FUNC;

public class StringMaskingFunctionTest extends BasePostgresTestContainer {

  private static final Logger log = LoggerFactory.getLogger(StringMaskingFunctionTest.class);
  private static final String CREATE_FUNCTION = "CREATE OR REPLACE FUNCTION %s.%s";

  @BeforeClass
  public static void addStringMaskingFunction() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA,
        MaskingConstants.MASK_STRING_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_STRING_FILE);

    PreparedStatement statement = getConnection().prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
  }

  @Test
  public void testStringMaskWithDefaultParameters() throws SQLException {
    Statement stmt = getConnection().createStatement();
    // When only string is given
    String selectquery = "Select " + SCHEMA + "." + MASK_STRING_FUNC + "('abcdefghij') as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("xxxxxxxxxx", rs.getString(1));
  }

  @Test
  public void testStringMaskWithSpecifiedSeparator() throws SQLException {
    Statement stmt = getConnection().createStatement();
    // When string and masking pattern is given
    String selectquery = "Select " + SCHEMA + "." + MASK_STRING_FUNC
        + "('abcdefghij','*') as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("**********", rs.getString(1));

  }

  @Test
  public void testStringMaskWithSeparatorandPrefixLength() throws SQLException {
    Statement stmt = getConnection().createStatement();
    // When string, masking pattern, prefix length is given
    String selectquery = "Select " + SCHEMA + "." + MASK_STRING_FUNC
        + "('abcdefghij','#',3) as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("abc#######", rs.getString(1));
  }

  @Test
  public void testStringMaskWithSeparatorPrefixSuffixLength() throws SQLException {
    Statement stmt = getConnection().createStatement();
    // When string, masking pattern, prefix  and suffix length is given
    String selectquery = "Select " + SCHEMA + "." + MASK_STRING_FUNC
        + "('abcdefghij','x',3,2) as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("abcxxxxxij", rs.getString(1));
  }
}

