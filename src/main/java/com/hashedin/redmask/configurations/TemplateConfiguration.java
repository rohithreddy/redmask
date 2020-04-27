package com.hashedin.redmask.configurations;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class TemplateConfiguration {

  private static final Logger log = LoggerFactory.getLogger(TemplateConfiguration.class);

  private static final String TEMPLATE_DIR = "src/main/resources/templates";

  private final Configuration config;

  public TemplateConfiguration() {

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

    // Specify the source where the template files come from.
    try {
      cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_DIR));
    } catch (IOException e) {
      log.error("Template Directory Not Found", e);
    }
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    this.config = cfg;
  }

  public Configuration getConfig() {
    return config;
  }

}
