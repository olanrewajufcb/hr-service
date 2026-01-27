package com.emis.hrservice.exceptions;

public class ResourceTimeoutException extends RuntimeException {
  public ResourceTimeoutException(String noFacilitiesFound, Throwable cause) {
    super(noFacilitiesFound, cause);
  }
}
