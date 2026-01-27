package com.emis.hrservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SchoolStatus {
    ACTIVE,
    INACTIVE,
    CLOSED,
    SHUTDOWN;

    @JsonCreator
    public static SchoolStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return SchoolStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value for SchoolStatus: '" + value +
                            "'. Accepted values are: ACTIVE, INACTIVE, CLOSED, SHUTDOWN"
            );
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }

    }
