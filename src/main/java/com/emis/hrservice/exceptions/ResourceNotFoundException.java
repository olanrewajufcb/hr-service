package com.emis.hrservice.exceptions;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException{
  private final String fieldName;
  private final Object searchValue;

  public ResourceNotFoundException(String message) {
    super(message);
    this.fieldName = null;
    this.searchValue = null;
  }

  public ResourceNotFoundException(String message, String fieldName, Object searchValue) {
    super(message);
    this.fieldName = fieldName;
    this.searchValue = searchValue;
  }
}
