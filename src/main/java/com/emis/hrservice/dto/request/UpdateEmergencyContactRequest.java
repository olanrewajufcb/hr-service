package com.emis.hrservice.dto.request;

public record UpdateEmergencyContactRequest(
        String schoolCode,
        String name,
        String relationship,
        String phoneNumber

) {}
