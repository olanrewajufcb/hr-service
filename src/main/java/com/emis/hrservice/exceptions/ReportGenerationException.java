package com.emis.hrservice.exceptions;

public class ReportGenerationException extends RuntimeException {
  public ReportGenerationException(String msg, Throwable ex) {
    super(msg, ex);
  }
}
