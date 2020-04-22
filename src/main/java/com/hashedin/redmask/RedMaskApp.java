package com.hashedin.redmask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.redmask.configurations.InvalidParameterValueException;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MissingParameterException;
import com.hashedin.redmask.service.MaskingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(description = "Redmask Tool",
  name = "redmask",
  mixinStandardHelpOptions = true,
  version = "redmask 1.0")
public class RedMaskApp implements Callable<Integer>  {

  private static final Logger log = LogManager.getLogger(RedMaskApp.class);

  @Option(names = {"-f", "--configfilepath"}, required = true,
      description = "Complete file path of json containing masking configurations.")
  private String configFilePath;

  @Option(names = {"-r", "--dryrun"},
      description = "When true, this will just generates sql file with required queries. "
          + "It will not make any chnages to DB.")
  private boolean dryRun = false;

  public static void main(String[] args) throws IOException {
    log.info("Starting redmask application.");
    System.exit(new CommandLine(new RedMaskApp()).execute(args));
  }

  @Override
  public Integer call() throws Exception {

    MaskConfiguration config = null;

    try {
      config = new ObjectMapper().readValue(new File(configFilePath), MaskConfiguration.class);
    } catch (Exception ex) {
      log.error("Exception while reading config.json file: " + ex);
    }
    try {
      MaskingService service = new MaskingService(config, dryRun);
      service.generateSqlQueryForMasking();
      service.executeSqlQueryForMasking();
      log.info("Closing redmask application.");
      return 0;
    } catch (MissingParameterException | InvalidParameterValueException ex) {
      log.error("Error occurred while executing", ex);
    }
    return 0;
  }

}
