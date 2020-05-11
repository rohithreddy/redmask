package com.hashedin.redmask;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashedin.redmask.config.MaskingConstants;

import static com.hashedin.redmask.config.MaskingConstants.MASK_CARD_FUNC;
import static com.hashedin.redmask.config.MaskingConstants.MASK_EMAIL_FUNC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SpecialMaskingFunctionTest extends BasePostgresTestContainer {

  private static final Logger log = LoggerFactory.getLogger(SpecialMaskingFunctionTest.class);
  private static final String CREATE_FUNCTION = "CREATE OR REPLACE FUNCTION %s.%s";

  @Test
  public void testCardMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_NUMBERS_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_NUMBERS_FILE)
        + String.format(CREATE_FUNCTION, SCHEMA, MASK_CARD_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_CARD_FILE);
    PreparedStatement statement = getConnection().prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
    Statement stmt = getConnection().createStatement();

    String selectquery = "Select " + SCHEMA + "."+MASK_CARD_FUNC+"('1234567812345678') as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("xxxxxxxxxxxx5678", rs.getString(1));


    selectquery = "Select " + SCHEMA + "."+MASK_CARD_FUNC+"('1234567812345678', 'first') as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("1234xxxxxxxxxxxx", rs.getString(1));


    selectquery = "Select " + SCHEMA + "."+MASK_CARD_FUNC+"('1234567812345678', 'firstnlast', '', 4, 4) as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("1234xxxxxxxx5678", rs.getString(1));

    selectquery = "Select " + SCHEMA + "."+MASK_CARD_FUNC+"('1234-5678-1234-5678', 'last', '-', 5) as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("xxxx-xxxx-xxx4-5678", rs.getString(1));


    selectquery = "Select " + SCHEMA + "."+MASK_CARD_FUNC+"('1234-5678-1234-5678','first','-',5) as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("1234-5xxx-xxxx-xxxx", rs.getString(1));

    selectquery = "Select " + SCHEMA + "."+MASK_CARD_FUNC+"('1234-5678-1234-5678','firstnlast','-',5,7) as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("1234-5xxx-x234-5678", rs.getString(1));
  }

  @Test
  public void testEmailMask() throws SQLException, IOException {
    String createFunctionQuery = String.format(CREATE_FUNCTION, SCHEMA, MaskingConstants.MASK_STRING_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_STRING_FILE)
        + String.format(CREATE_FUNCTION, SCHEMA, MASK_EMAIL_FUNC)
        + getFunctionQuery(MaskingConstants.MASK_EMAIL_FILE);
    PreparedStatement statement = getConnection().prepareStatement(createFunctionQuery);
    statement.execute();
    statement.close();
    Statement stmt = getConnection().createStatement();

    String selectquery = "Select " + SCHEMA + "." + MASK_EMAIL_FUNC + "('sample_user@email.com') as masked";
    ResultSet rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("xxxxxxxxxxx@email.com", rs.getString(1));

    selectquery = "Select " + SCHEMA + "." + MASK_EMAIL_FUNC + "('sample_user@email.com','firstN') as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("sampxxxxxxxxxxxxxxxxx", rs.getString(1));

    selectquery = "Select " + SCHEMA + "." + MASK_EMAIL_FUNC + "('sample_user@email.com','firstN',6) as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("samplexxxxxxxxxxxxxxx", rs.getString(1));

    selectquery = "Select " + SCHEMA + "." + MASK_EMAIL_FUNC + "('sample_user@email.com','firstndomain') as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("sxxxxxxxxxx@email.com", rs.getString(1));

    selectquery = "Select " + SCHEMA + "." + MASK_EMAIL_FUNC + "('sample_user@email.com','nonspecialcharacter') as masked";
    rs = stmt.executeQuery(selectquery);
    rs.next();
    Assert.assertEquals("xxxxxx_xxxx@xxxxx.xxx", rs.getString(1));
  }
}
