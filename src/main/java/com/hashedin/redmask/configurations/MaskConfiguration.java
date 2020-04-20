package com.hashedin.redmask.configurations;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.hashedin.redmask.configurations.MaskingConstants;

public class MaskConfiguration {

  private String host;
  private String port;
  private String database;
  private String user;
  private List<MaskingRule> rules;

  // Get these credentials from environment variable.
  private final String superUser;
  private final String superUserPassword;
  private final String username;
  private final String userPassword;

  @JsonIgnore
  private TemplateConfiguration templateConfig;

  public MaskConfiguration() throws IOException {
    this.superUser = System.getenv(MaskingConstants.DB_SUPER_USER);
    this.superUserPassword = System.getenv(MaskingConstants.DB_SUPER_USER_PASSWORD);
    this.username = System.getenv(MaskingConstants.DB_USER);
    this.userPassword = System.getenv(MaskingConstants.DB_USER_PASSWORD);
    this.templateConfig = new TemplateConfiguration();
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
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

  public String getDatabase() {
    return database;
  }

  public List<MaskingRule> getRules() {
    return rules;
  }

  public TemplateConfiguration getTemplateConfig() {
    return templateConfig;
  }

  @Override
  public String toString() {
    return "MaskConfiguration [host=" + host + ", port=" + port + ", database=" + database + ", user=" + user
        + ", rules=" + rules + ", templateConfig=" + templateConfig + "]";
  }
}
