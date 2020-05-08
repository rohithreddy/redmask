package com.hashedin.redmask.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class RedshiftPOC {

  private RedshiftPOC() {}
  
  private static final String REDSHIFT_DB_URL = 
      "jdbc:redshift://redshift-cluster-1.coxloxnpdxcw.us-east-1."
      + "redshift.amazonaws.com:5439/redmask";
  private static final String MASTER_USERNAME = "hashedin";
  private static final String MASTER_PASSWORD = "Hasher123";

  public static void main(String[] args) throws SQLException {
    Connection conn = null;
    Statement stmt = null;
    try {
      //Dynamically load driver at runtime.
      //Redshift JDBC 4.1 driver: com.amazon.redshift.jdbc41.Driver
      //Redshift JDBC 4 driver: com.amazon.redshift.jdbc4.Driver
      Class.forName("com.amazon.redshift.jdbc.Driver");

      //Open a connection and define properties.
      System.out.println("Connecting to database...");
      Properties props = new Properties();

      //Uncomment the following line if using a keystore.
      //props.setProperty("ssl", "true");
      props.setProperty("user", MASTER_USERNAME);
      props.setProperty("password", MASTER_PASSWORD);
      conn = DriverManager.getConnection(REDSHIFT_DB_URL, props);

      //Try a simple query.
      System.out.println("dropping table customer if it exists");
      stmt = conn.createStatement();
      String sql;
      sql = "DROP TABLE IF EXISTS customer CASCADE;";
      stmt.execute(sql);

      sql = "CREATE TABLE customer(\n" 
          + "  name text,\n"
          + "  email text,\n" 
          + "  age integer,\n"
          + "  DOB date,\n"
          + "  interest float,\n"
          + "  card text\n);";
      stmt.execute(sql);

      sql = "insert into customer "
          + "VALUES ('User Alpha','useralpha@email.com',1,'2019-07-26',5.4,'1234-5679-8723-8789');";
      stmt.execute(sql);

      sql = "insert into customer "
          + "VALUES ('User Beta','userbeta@email.com',2,'2019-06-25',6.4,'1234-5679-3478-6872');";
      stmt.execute(sql);

      sql = " create or replace view "
          + "mymaskedcustomer as "
          + "select f_python_string_mask(name,'*',2,1) as name, email, age from customer; ";
      stmt.execute(sql);

      sql = "select * from mymaskedcustomer";
      ResultSet rs = stmt.executeQuery(sql);
      //Get the data from the result set.
      while (rs.next()) {
        //Retrieve two columns.
        String name = rs.getString("name");
        String email = rs.getString("email");
        int age = rs.getInt("age");
        //Display values.
        System.out.println(name + ":" + email + "|" + age);
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (Exception ex) {
      //For convenience, handle all errors here.
      ex.printStackTrace();
    } finally {
      //Finally block to close resources.
      try {
        if (stmt != null) {
          stmt.close();
        }
          
      } catch (Exception ex) {
      } // nothing we can do
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    System.out.println("Finished connectivity test.");
  }
}