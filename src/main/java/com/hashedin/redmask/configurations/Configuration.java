package com.hashedin.redmask.configurations;

import java.util.List;

public class Configuration {

  private String host;
  
  private String port;
  
  private String database;
  
  private String user;
  
  private List<MaskingRule> rules;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
  
  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public List<MaskingRule> getRules() {
    return rules;
  }

  public void setRules(List<MaskingRule> rules) {
    this.rules = rules;
  }
  
  @Override
  public String toString() {
    return "Configuration [host=" + host + ", port=" + port + ", database=" + database + ", user=" + user + ", rules="
        + rules + "]";
  }
  
}
