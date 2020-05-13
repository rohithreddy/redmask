package com.hashedin.redmask.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.exception.RedmaskRuntimeException;

public abstract class DataMasking {

  private static final Logger log = LoggerFactory.getLogger(DataMasking.class);

  // TODO :Abstract validate credentials, table, column etc config.

  public abstract void generateSqlQueryForMasking() throws RedmaskConfigException;

  public abstract void executeSqlQueryForMasking() throws IOException, ClassNotFoundException;

  public void grantAccessToMaskedData(FileWriter writer,
      String maskingFunSchema, String user) throws IOException {
    log.info("Required permission have been granted to the specified user.");
    writer.append("\n\n-- Grant access to current user on schema: "
        + maskingFunSchema + ".\n");
    writer.append("GRANT USAGE ON SCHEMA " + maskingFunSchema
        + " TO " + user + ";");
    writer.append("\n\n-- Grant access to current user on schema: " + user + ".\n");
    writer.append("GRANT ALL PRIVILEGES ON ALL TABLES IN "
        + "SCHEMA " + user + " TO " + user + ";");
    writer.append("\nGRANT USAGE ON SCHEMA " + user
        + " TO " + user + ";");
  }
  
  public void executeSqlScript(String url, Properties props,
      File scriptFilePath) throws IOException {
    Reader reader = null;
    try (Connection CONN = DriverManager.getConnection(url, props)) {
      //Initialize the script runner
      ScriptRunner sr = new ScriptRunner(CONN);

      //Creating a reader object
      reader = new BufferedReader(
          new FileReader(scriptFilePath));

      //Running the script
      sr.setSendFullScript(true);
      sr.runScript(reader);
    } catch (SQLException ex) {
      throw new RedmaskRuntimeException(
          String.format("DB Connection error while executing masking sql "
              + "query from file: {} using super username: {}",
              scriptFilePath, props.get("user")), ex);
    } catch (FileNotFoundException ex) {
      throw new RedmaskRuntimeException(
          String.format("Masking sql query file {} not found", scriptFilePath.getName()), ex);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

}
