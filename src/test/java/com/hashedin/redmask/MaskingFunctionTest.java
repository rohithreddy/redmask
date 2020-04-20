package com.hashedin.redmask;

import com.hashedin.redmask.configurations.MaskingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MaskingFunctionTest extends BasePostgresTestContainer {

  private static final Logger log = LogManager.getLogger(MaskingFunctionTest.class);

  private static final String CREATE_FUNCTION = "CREATE OR REPLACE FUNCTION %s.%s";

  @Test
  public void testStringMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA,
          MaskingConstants.MASK_STRING_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_STRING_FILE);

      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();

      Statement stmt = connection.createStatement();
      // When only string is given
      String selectquery = "Select redmask.anonymize('abcdefghij') as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("xxxxxxxxxx", rs.getString(1));

      // When string and masking pattern is given
      selectquery = "Select redmask.anonymize('abcdefghij','*') as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("**********", rs.getString(1));

      // When string, masking pattern, prefix length is given
      selectquery = "Select redmask.anonymize('abcdefghij','#',3) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("abc#######", rs.getString(1));

      // When string, masking pattern, prefix  and suffix length is given
      selectquery = "Select redmask.anonymize('abcdefghij','x',3,2) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("abcxxxxxij", rs.getString(1));

    } catch (IOException | SQLException e) {
      log.error("Exception while executing testStringMask test {}", e);
    }
  }

  @Test
  public void testCardMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_NUMBERS_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_NUMBERS_FILE)
          + String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_CARD_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_CARD_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      Statement stmt = connection.createStatement();

      String selectquery = "Select redmask.cardmask('1234567812345678') as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("xxxxxxxxxxxx5678", rs.getString(1));


      selectquery = "Select redmask.cardmask('1234567812345678','first') as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("1234xxxxxxxxxxxx", rs.getString(1));


      selectquery = "Select redmask.cardmask('1234567812345678','firstnlast','',4,4) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("1234xxxxxxxx5678", rs.getString(1));

      selectquery = "Select redmask.cardmask('1234-5678-1234-5678','last','-',5) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("xxxx-xxxx-xxx4-5678", rs.getString(1));


      selectquery = "Select redmask.cardmask('1234-5678-1234-5678','first','-',5) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("1234-5xxx-xxxx-xxxx", rs.getString(1));

      selectquery = "Select redmask.cardmask('1234-5678-1234-5678','firstnlast','-',5,7) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("1234-5xxx-x234-5678", rs.getString(1));

    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testEmailMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_STRING_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_STRING_FILE)
          + String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_EMAIL_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_EMAIL_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      Statement stmt = connection.createStatement();

      String selectquery = "Select redmask.emailmask('sample_user@email.com') as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("xxxxxxxxxxx@email.com", rs.getString(1));

      selectquery = "Select redmask.emailmask('sample_user@email.com','firstN') as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("sampxxxxxxxxxxxxxxxxx", rs.getString(1));

      selectquery = "Select redmask.emailmask('sample_user@email.com','firstN',6) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("samplexxxxxxxxxxxxxxx", rs.getString(1));

      selectquery = "Select redmask.emailmask('sample_user@email.com','firstndomain') as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("sxxxxxxxxxx@email.com", rs.getString(1));

      selectquery = "Select redmask.emailmask('sample_user@email.com','nonspecialcharacter') as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("xxxxxx_xxxx@xxxxx.xxx", rs.getString(1));


    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testFixedSizeIntegerMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FILE)
          + String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_FIXED_SIZE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_INTEGER_FIXED_SIZE_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      Statement stmt = connection.createStatement();
      String selectquery = "Select redmask.generate(1,5) as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertTrue((rs.getInt(1) > 9999));
      Assert.assertTrue(rs.getInt(1) < 100000);


      selectquery = "Select redmask.generate(1,6) as masked";
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertTrue((rs.getInt(1) > 99999));
      Assert.assertTrue(rs.getInt(1) < 1000000);
    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testFixedValueIntegerMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_FIXED_VALUE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_INTEGER_FIXED_VALUE_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      int integerColumnValue = 1;
      Statement stmt = connection.createStatement();
      String selectquery = "Select redmask.replaceby(" + integerColumnValue + ",5) as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals(5, rs.getInt(1));

    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testFixedValueFloatMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_FLOAT_FIXED_VALUE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_FLOAT_FIXED_VALUE_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      int integerColumnValue = 1;
      Statement stmt = connection.createStatement();
      String selectquery = "Select redmask.replaceby(" + integerColumnValue + ",4.567) as masked";
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals(4.567, rs.getFloat(1), 0.01);

    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testIntegerWithinRangeMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      int integerColumnValue = 1;
      int start = 90;
      int end = 100;
      Statement stmt = connection.createStatement();
      String selectquery = String.format("Select redmask.random_int_between(%d,%d,%d) as masked",
          integerColumnValue, start, end);
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertTrue(90 <= rs.getInt(1));
      Assert.assertTrue(100 >= rs.getInt(1));


    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testIntegerRangeMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_RANGE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_INTEGER_RANGE_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();

      int integerColumnValue = 1;
      int step = 20;
      Statement stmt = connection.createStatement();
      String selectquery = String.format("Select redmask.range_int8(%d) as masked", integerColumnValue);
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[0,10)",rs.getString(1));

      selectquery = String.format("Select redmask.range_int8(%d, %d) as masked", integerColumnValue, step);
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[0,20)",rs.getString(1));

      integerColumnValue=35;
      selectquery = String.format("Select redmask.range_int8(%d, %d) as masked", integerColumnValue, step);
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[20,40)",rs.getString(1));

    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testBigIntegerRangeMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_BIGINT_RANGE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_BIGINT_RANGE_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      int integerColumnValue = 1;
      int step = 20;
      Statement stmt = connection.createStatement();
      String selectquery = String.format("Select redmask.range_int8(%d) as masked", integerColumnValue);
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[0,10)",rs.getString(1));

      selectquery = String.format("Select redmask.range_int8(%d, %d) as masked", integerColumnValue, step);
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[0,20)",rs.getString(1));

      integerColumnValue=1234567899;
      selectquery = String.format("Select redmask.range_int8(%d, %d) as masked", integerColumnValue, step);
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[1234567880,1234567900)",rs.getString(1));


    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  @Test
  public void testNumericRangeMask() {
    try {
      String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_RANGE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_INTEGER_RANGE_FILE)
          + String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_NUMERIC_RANGE_FUNC)
          + getFunctionQuery(MaskingConstants.MASK_NUMERIC_RANGE_FILE);
      PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
      statement.execute();
      statement.close();
      int integerColumnValue = 1;
      int step = 20;
      Statement stmt = connection.createStatement();
      String selectquery = String.format("Select redmask.range_numeric(%d) as masked", integerColumnValue);
      ResultSet rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[0,10)",rs.getString(1));

      selectquery = String.format("Select redmask.range_numeric(%d, %d) as masked", integerColumnValue, step);
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[0,20)",rs.getString(1));

      integerColumnValue=35;
      selectquery = String.format("Select redmask.range_numeric(%d, %d) as masked", integerColumnValue, step);
      rs = stmt.executeQuery(selectquery);
      rs.next();
      Assert.assertEquals("[20,40)",rs.getString(1));
    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

}
