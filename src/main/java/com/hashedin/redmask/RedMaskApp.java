package com.hashedin.redmask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.Configuration;
import com.hashedin.redmask.service.MaskingService;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Redmask Tool",
  name = "redmask",
  mixinStandardHelpOptions = true,
  version = "redmask 1.0")
public class RedMaskApp implements Callable<Integer>  {

  private static final Logger log = LogManager.getLogger(RedMaskApp.class);

  @Option(names = {"-f", "--configfilepath"}, required = true,
      description = "Complete file path of json containing masking configurations.")
  private String configFilePath;

  public static void main( String[] args ) throws IOException {
    log.info("Starting redmask application.");
    System.exit(new CommandLine(new RedMaskApp()).execute(args));
    log.info("Closing redmask application.");
  }

  @Override
  public Integer call() throws Exception {

    Configuration config = null;

    try {
      config = new ObjectMapper().readValue(new File(configFilePath), Configuration.class);
    } catch (Exception ex) {
      log.error("Exception while reading config.json file: " + ex);
    }
    
    MaskingService service = new MaskingService(config);
    service.createSchemaAndView();
    
    return 0;
  }

}
