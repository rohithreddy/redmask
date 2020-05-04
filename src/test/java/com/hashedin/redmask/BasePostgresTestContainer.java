package com.hashedin.redmask;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BasePostgresTestContainer {

  private static final Logger log = LoggerFactory.getLogger(BasePostgresTestContainer.class);
  private static final String BASE_FILE_PATH= "src/main/resources/";

  protected static final String SCHEMA = "redmask";
  protected static Connection connection;
  
  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer();

  @BeforeClass
  public static void setup() {
    try {
      connection = DriverManager.getConnection(
          postgres.getJdbcUrl(),
          postgres.getUsername(),
          postgres.getPassword()
          );
      // Create redmask schema.
      log.info("Started Postgres docker Test Container and created connection to the test DB.");
      try(PreparedStatement statement = connection.prepareStatement(
          "CREATE SCHEMA IF NOT EXISTS " + SCHEMA)){
        statement.execute();
      }
    } catch (SQLException ex) {
      log.error("Exception while making connction to  postgres test container {}", ex);
    }
  }

  @AfterClass
  public static void tearDown(){
    try {
      connection.close();
      postgres.close();
      log.info("Closed postgres connection and test container.");
    } catch (SQLException ex) {
      log.error("Exception while closing connction to  postgres test container {}", ex);
    }
  }
  
  protected static String getFunctionQuery(String filePath) throws IOException {
    // Creating a reader object
    return IOUtils.toString(
        new FileInputStream(BASE_FILE_PATH + filePath),StandardCharsets.UTF_8);
  }
}
