package com.hashedin.redmask.factory;

import java.io.IOException;

import com.hashedin.redmask.exception.RedmaskConfigException;

public abstract class DataMasking {

  // TODO :Abstract validate credentials, table, column etc config.
  
  public abstract void generateSqlQueryForMasking() throws RedmaskConfigException;
  
  public abstract void executeSqlQueryForMasking() throws IOException, ClassNotFoundException;
  
}
