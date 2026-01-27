package com.emis.hrservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SchoolLevel {

    PRIMARY,
    SECONDARY,
    OTHER;


    @JsonCreator
    public static SchoolLevel fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return SchoolLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value for SchoolLevel: '" + value +
                            "'. Accepted values are:  PRIMARY, SECONDARY, OTHER"
            );
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
