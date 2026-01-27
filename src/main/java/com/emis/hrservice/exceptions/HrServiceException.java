package com.emis.hrservice.exceptions;

public class HrServiceException extends RuntimeException {
  public HrServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
