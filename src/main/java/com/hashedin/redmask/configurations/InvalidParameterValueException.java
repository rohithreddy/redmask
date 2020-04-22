package com.hashedin.redmask.configurations;

public class InvalidParameterValueException extends RuntimeException {
  public InvalidParameterValueException(String errMessage) {
    super(errMessage);
  }
}
