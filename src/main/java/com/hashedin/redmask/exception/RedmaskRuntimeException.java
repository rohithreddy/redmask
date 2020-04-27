package com.hashedin.redmask.exception;

public class RedmaskRuntimeException extends RuntimeException {

  public RedmaskRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public RedmaskRuntimeException(String message) {
    super(message);
  }

  public RedmaskRuntimeException(Throwable cause) {
    super(cause);
  }

}
