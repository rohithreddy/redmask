package com.hashedin.redmask.exception;

/**
 * A Custom runtime exception class to handle all 
 * configuration related exceptions.
 */
public class RedmaskConfigException extends RuntimeException {

  public RedmaskConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  public RedmaskConfigException(String message) {
    super(message);
  }

  public RedmaskConfigException(Throwable cause) {
    super(cause);
  }

}
