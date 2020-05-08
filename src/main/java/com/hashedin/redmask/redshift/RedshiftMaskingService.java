package com.hashedin.redmask.redshift;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.exception.RedmaskRuntimeException;
import com.hashedin.redmask.factory.DataMasking;

public class RedshiftMaskingService extends DataMasking {
  
  private static final Logger log = LoggerFactory.getLogger(RedshiftMaskingService.class);

  private final MaskConfiguration config;
  private String url = "jdbc:redshift://";
  private final boolean dryRunEnabled;

  // This temp would contain queries to create masked data.
  private final File tempFilePath;

  public RedshiftMaskingService(MaskConfiguration config, boolean dryRunEnabled) {
    super();
    this.config = config;
    this.url = url + "redshift-cluster-1.coxloxnpdxcw.us-east-1.redshift.amazonaws.com" 
        + config.getPort() + "/" + config.getDatabase();
    this.dryRunEnabled = dryRunEnabled;
    this.tempFilePath = createMaskingSqlFile();
  }

  @Override
  public void generateSqlQueryForMasking() {
    // TODO Auto-generated method stub

  }

  @Override
  public void executeSqlQueryForMasking() throws IOException {
    // TODO Auto-generated method stub

  }

  private File createMaskingSqlFile() {
    // create a temp .sql file
    File sqlFile = null;
    try {
      sqlFile = File.createTempFile("redmask-masking", ".sql");
      log.info("Created a temp file at location: {}", sqlFile.getAbsolutePath());
    } catch (IOException ex) {
      throw new RedmaskRuntimeException(
          "Error while creating temporary file \'redmask-masking.sql\'", ex);
    }
    return sqlFile;
  }

}
