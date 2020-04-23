package com.hashedin.redmask.configurations;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.util.List;

import static com.hashedin.redmask.configurations.MaskingConstants.DB_SUPER_USER;
import static com.hashedin.redmask.configurations.MaskingConstants.DB_SUPER_USER_PASSWORD;
import static com.hashedin.redmask.configurations.MaskingConstants.DB_USER;
import static com.hashedin.redmask.configurations.MaskingConstants.DB_USER_PASSWORD;

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
    this.superUser = System.getenv(DB_SUPER_USER);
    this.superUserPassword = System.getenv(DB_SUPER_USER_PASSWORD);
    this.username = System.getenv(DB_USER);
    this.userPassword = System.getenv(DB_USER_PASSWORD);
    this.templateConfig = new TemplateConfiguration();
  }

  /**
   * This constructor will only be used for Integration Testing
   */
  public MaskConfiguration(
      String superUser,
      String superUserPassword,
      String username,
      String userPassword,
      String host,
      String port,
      String database,
      String user) throws IOException {

    this.superUser = superUser;
    this.superUserPassword = superUserPassword;
    this.username = username;
    this.userPassword = userPassword;
    this.templateConfig = new TemplateConfiguration();
    this.host = host;
    this.port = port;
    this.database = database;
    this.user = user;
  }

  /**
   * This function is used to set masking rules only during integration testing
   *
   * @param rules List of masking rule for the following table
   */
  public void setRules(List<MaskingRule> rules) {
    this.rules = rules;
  }

  public void setTemplateConfig(TemplateConfiguration templateConfig) {
    this.templateConfig = templateConfig;
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
