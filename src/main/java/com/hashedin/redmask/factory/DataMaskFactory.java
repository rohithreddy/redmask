package com.hashedin.redmask.factory;

import com.hashedin.redmask.common.DataMasking;
import com.hashedin.redmask.config.MaskConfiguration;
import com.hashedin.redmask.service.PostgresMaskingService;
import com.hashedin.redmask.service.RedshiftMaskingService;
import com.hashedin.redmask.service.SnowflakeMaskingService;

public class DataMaskFactory {

  private DataMaskFactory() {
    //  No use
  }

  public static DataMasking buildDataMask(
      MaskConfiguration config,
      boolean dryRunMode,
      String maskingMode) {

    DataMasking dataMasking = null;
    switch (config.getDbType()) {
      case POSTGRES:
        dataMasking = new PostgresMaskingService(config, dryRunMode, maskingMode);
        break;
      case REDSHIFT:
        dataMasking = new RedshiftMaskingService(config, dryRunMode, maskingMode);
        break;
      case SNOWFLAKE:
        // Create DataMasking instance for Snowflake.
        dataMasking = new SnowflakeMaskingService(config, dryRunMode, maskingMode);
        break;
      default:
        // throw unsupported data base type.
        break;
    }
    return dataMasking;
  }
}
