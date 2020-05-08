package com.hashedin.redmask.factory;

import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.postgres.PostgresMaskingService;

public class DataMaskFactory {

  private DataMaskFactory() {
    //  No use
  }

  public static DataMasking buildDataMask(MaskConfiguration config, boolean dryRunMode) {
    DataMasking dataMasking = null;
    switch (config.getDbType()) {
      case POSTGRES:
        dataMasking = new PostgresMaskingService(config, dryRunMode);
        break;
      case REDSHIFT:
        dataMasking = new PostgresMaskingService(config, dryRunMode);
        break;
      case SNOWFLAKE:
        dataMasking = new PostgresMaskingService(config, dryRunMode);
        break;
      default:
        // throw unsupported data base type.
        break;
    }
    return dataMasking;
  }
}
