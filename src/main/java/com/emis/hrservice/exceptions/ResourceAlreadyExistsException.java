package com.emis.hrservice.exceptions;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {
    private final String fieldName;
    private final Object rejectedValue;

    public ResourceAlreadyExistsException(String message) {
        super(message);
        this.fieldName = null;
        this.rejectedValue = null;
    }

    public ResourceAlreadyExistsException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }
}
