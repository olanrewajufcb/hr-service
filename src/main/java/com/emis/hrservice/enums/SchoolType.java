package com.emis.hrservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SchoolType {
    PRIVATE,
    PUBLIC,
    OTHER;

    @JsonCreator
    public static SchoolType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return SchoolType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value for SchoolType: '" + value +
                            "'. Accepted values are: PRIVATE, PUBLIC, OTHER"
            );
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
