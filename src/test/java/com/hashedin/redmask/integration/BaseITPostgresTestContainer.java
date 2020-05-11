package com.hashedin.redmask.integration;

import org.junit.AfterClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;

public class BaseITPostgresTestContainer {

  private static final Logger log = LoggerFactory.getLogger(BaseITPostgresTestContainer.class);

  protected static final String TEST_DATA_FILE = "src/test/resources/HelperSQL/InitializeDB.sql";
  // File to add more data in the tables.
  protected static final String INSERT_DATA_FILE = "src/test/resources/HelperSQL/InsertDB.sql";
  protected static final String UPDATE_DATA_FILE = "src/test/resources/HelperSQL/UpdateDB.sql";
  protected static final String DELETE_DATA_FILE = "src/test/resources/HelperSQL/DeleteDB.sql";
  
  protected static final String DEV_USER = "developer";
  protected static final String DEV_USER_PASSWORD = "password";
  protected static final String TABLE_NAME = "customer";
  protected static final String TABLE_NAME_2 = "cashier";

  protected static final String SCHEMA = "redmask";
  protected static Connection connection;

  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

  @AfterClass
  public static void tearDown() {
    try {
      postgres.close();
      log.info("Closed postgres test container.");
    } catch (Exception ex) {
      log.error("Exception while closing postgres test container {}", ex);
    }
  }

}
