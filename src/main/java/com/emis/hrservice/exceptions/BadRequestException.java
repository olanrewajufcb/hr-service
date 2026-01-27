package com.emis.hrservice.exceptions;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private final String fieldName;
    private final Object rejectedValue;

    public BadRequestException(String message) {
        super(message);
        this.fieldName = null;
        this.rejectedValue = null;
    }

    public BadRequestException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }}
