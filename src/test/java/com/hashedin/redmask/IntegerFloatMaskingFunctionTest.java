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

public class IntegerFloatMaskingFunctionTest extends BasePostgresTestContainer {

  private static final Logger log = LogManager.getLogger(IntegerFloatMaskingFunctionTest.class);
  private static final String CREATE_FUNCTION = "CREATE OR REPLACE FUNCTION %s.%s";

  @Test
  public void testFixedSizeIntegerMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FILE)
        + String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_FIXED_SIZE_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_INTEGER_FIXED_SIZE_FILE);
    PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
    Statement stmt = connection.createStatement();
    String selectquery = "Select "+ SCHEMA+".generate(1,5) as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertTrue((rs.getInt(1) > 9999));
    Assert.assertTrue(rs.getInt(1) < 100000);


    selectquery = "Select "+ SCHEMA+".generate(1,6) as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertTrue((rs.getInt(1) > 99999));
    Assert.assertTrue(rs.getInt(1) < 1000000);
  }

  @Test
  public void testFixedValueIntegerMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_FIXED_VALUE_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_INTEGER_FIXED_VALUE_FILE);
    PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
    int integerColumnValue = 1;
    Statement stmt = connection.createStatement();
    String selectquery = "Select "+ SCHEMA+".replaceby(" + integerColumnValue + ",5) as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals(5, rs.getInt(1));
  }

  @Test
  public void testFixedValueFloatMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_FLOAT_FIXED_VALUE_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_FLOAT_FIXED_VALUE_FILE);
    PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
    int integerColumnValue = 1;
    Statement stmt = connection.createStatement();
    String selectquery = "Select "+ SCHEMA+".replaceby(" + integerColumnValue + ",4.567) as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals(4.567, rs.getFloat(1), 0.01);
  }

  @Test
  public void testIntegerWithinRangeMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FILE);
    PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
    int integerColumnValue = 1;
    int start = 90;
    int end = 100;
    Statement stmt = connection.createStatement();
    String selectquery = String.format("Select "+ SCHEMA+".random_int_between(%d,%d,%d) as masked",
        integerColumnValue, start, end);
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertTrue(90 <= rs.getInt(1));
    Assert.assertTrue(100 >= rs.getInt(1));
  }

  @Test
  public void testIntegerRangeMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_INTEGER_RANGE_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_INTEGER_RANGE_FILE);
    PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();

    int integerColumnValue = 1;
    int step = 20;
    Statement stmt = connection.createStatement();
    String selectquery = String.format("Select "+ SCHEMA+".range_int8(%d) as masked", integerColumnValue);
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[0,10)", rs.getString(1));

    selectquery = String.format("Select "+ SCHEMA+".range_int8(%d, %d) as masked", integerColumnValue, step);
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[0,20)", rs.getString(1));

    integerColumnValue = 35;
    selectquery = String.format("Select "+ SCHEMA+".range_int8(%d, %d) as masked", integerColumnValue, step);
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[20,40)", rs.getString(1));
  }

  @Test
  public void testBigIntegerRangeMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_BIGINT_RANGE_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_BIGINT_RANGE_FILE);
    PreparedStatement statement = connection.prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
    int integerColumnValue = 1;
    int step = 20;
    Statement stmt = connection.createStatement();
    String selectquery = String.format("Select "+ SCHEMA+".range_int8(%d) as masked", integerColumnValue);
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[0,10)", rs.getString(1));

    selectquery = String.format("Select "+ SCHEMA+".range_int8(%d, %d) as masked", integerColumnValue, step);
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[0,20)", rs.getString(1));

    integerColumnValue = 1234567899;
    selectquery = String.format("Select "+ SCHEMA+".range_int8(%d, %d) as masked", integerColumnValue, step);
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[1234567880,1234567900)", rs.getString(1));
  }

  @Test
  public void testNumericRangeMask() throws SQLException, IOException {
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
    String selectquery = String.format("Select "+ SCHEMA+".range_numeric(%d) as masked", integerColumnValue);
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[0,10)", rs.getString(1));

    selectquery = String.format("Select "+ SCHEMA+".range_numeric(%d, %d) as masked", integerColumnValue, step);
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[0,20)", rs.getString(1));

    integerColumnValue = 35;
    selectquery = String.format("Select "+ SCHEMA+".range_numeric(%d, %d) as masked", integerColumnValue, step);
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("[20,40)", rs.getString(1));
  }

}
