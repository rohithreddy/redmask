package com.hashedin.redmask.exception;

public class InvalidParameterValueException extends RuntimeException {
  public InvalidParameterValueException(String errMessage) {
    super(errMessage);
  }
}
