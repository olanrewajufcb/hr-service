package com.emis.hrservice.exceptions;

public class ResourceCreationException extends RuntimeException {
  public ResourceCreationException(String msg, Throwable ex) {
    super(msg, ex);
  }
}
