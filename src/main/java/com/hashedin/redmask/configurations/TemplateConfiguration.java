package com.hashedin.redmask.configurations;

import java.io.File;
import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class TemplateConfiguration {

  private static final String TEMPLATE_DIR = "src/main/resources/templates";

  private final Configuration config;

  public TemplateConfiguration() throws IOException {

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

    // Specify the source where the template files come from.
    cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_DIR));
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    this.config = cfg;
  }

  public Configuration getConfig() {
    return config;
  }

}
