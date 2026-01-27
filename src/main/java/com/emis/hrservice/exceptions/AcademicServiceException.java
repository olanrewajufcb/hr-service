package com.emis.hrservice.exceptions;

public class AcademicServiceException extends RuntimeException {
  public AcademicServiceException(String msg, Throwable err) {
    super(msg, err);
  }
}
