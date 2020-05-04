package com.hashedin.redmask.configurations;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateConfiguration {

  private static final Logger log = LoggerFactory.getLogger(TemplateConfiguration.class);

  // Path is src/main/resources/templates
  private static final String TEMPLATE_DIR = "/templates/";
  private static final String UTF_8 = "UTF-8";

  private final Configuration config;

  public TemplateConfiguration() {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
    cfg.setClassForTemplateLoading(this.getClass(), TEMPLATE_DIR);
    cfg.setDefaultEncoding(UTF_8);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    this.config = cfg;
  }

  public Configuration getConfig() {
    return config;
  }

}
