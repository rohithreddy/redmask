package com.hashedin.redmask.configurations;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MaskConfiguration {

  private String host;
  private String port;
  private String database;

  // Get these credentials from environment variable.
  private final String superUser;
  private final String superUserPassword;
  private final String username;
  private final String userPassword;

  private String user;
  private List<MaskingRule> rules;

  @JsonIgnore
  private TemplateConfiguration templateConfig;

  public MaskConfiguration() throws IOException {
    this.superUser = System.getenv("DB_SUPER_USER");
    this.superUserPassword = System.getenv("DB_SUPER_USER_PASSWORD");
    this.username = System.getenv("DB_USER");
    this.userPassword = System.getenv("DB_USER_PASSWORD");
    this.templateConfig = new TemplateConfiguration();
  }

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

  public String getSuperUser() {
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

  public TemplateConfiguration getTemplateConfig() {
    return templateConfig;
  }

  @Override
  public String toString() {
    return "Configuration [host=" + host + ", port=" + port + ", database=" + database + ", user=" + user + ", rules="
        + rules + "]";
  }

}
