package com.hashedin.redmask;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.Configuration;
import com.hashedin.redmask.service.MaskingService;

public class RedMaskApp {

  static Logger log = LogManager.getLogger(RedMaskApp.class);

  public static void main( String[] args ) {
    log.info("Starting redmask application.");

    String configFilePath = "";

    for (String val: args) {
      configFilePath = val;
    }
    
    ObjectMapper mapper = new ObjectMapper();
    Configuration config = null;

    try {
      config = mapper.readValue(new File(configFilePath), Configuration.class);
    } catch (Exception ex) {
      log.error("Exception while reading config.json file: " + ex);
    }
    
    MaskingService service = new MaskingService(config);
    service.createSchemaAndView();
    
    log.info("Closing redmask application.");
  }

}
