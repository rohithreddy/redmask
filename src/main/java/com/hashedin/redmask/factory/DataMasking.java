package com.hashedin.redmask.factory;

import java.io.IOException;

public abstract class DataMasking {

  // Validate credentials, table, column etc config.
  
  public abstract void generateSqlQueryForMasking();
  
  public abstract void executeSqlQueryForMasking() throws IOException, ClassNotFoundException;
  
}
