package com.hashedin.redmask.configurations;

public class DataBaseCredentials {
  
  private String superUser;
  
  private String superUserPassword;
  
  private String username;
  
  private String userPassword;
  
  public DataBaseCredentials(){
    this.superUser = System.getenv("DB_SUPER_USER");
    this.superUserPassword = System.getenv("DB_SUPER_USER_PASSWORD");
    this.username = System.getenv("DB_USER");
    this.userPassword = System.getenv("DB_USER_PASSWORD");
  }

  public String getSuperUserName() {
    return superUser;
  }

  public String getSuperUserPassword() {
    return superUserPassword;
  }

  public String getUsername() {
    return username;
  }

  public String getUserPassword() {
    return userPassword;
  }
  
}
