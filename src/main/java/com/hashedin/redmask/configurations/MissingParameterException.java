package com.hashedin.redmask.configurations;

public class MissingParameterException extends RuntimeException {
  public MissingParameterException(String errMessage) {
    super(errMessage);
  }
}
