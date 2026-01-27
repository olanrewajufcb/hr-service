package com.emis.hrservice.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonConversionException extends RuntimeException {
  public JsonConversionException(String msg, JsonProcessingException ex) {
    super(msg, ex);
  }
}
