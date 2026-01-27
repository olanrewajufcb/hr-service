package com.emis.hrservice.exceptions;

public class ValidationException extends RuntimeException{
  public ValidationException(String msg) {
    super(msg);
  }
}
