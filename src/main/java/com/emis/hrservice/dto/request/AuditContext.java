package com.emis.hrservice.dto.request;

public record AuditContext(
        String userId,
        String role,
        String source
) {}
