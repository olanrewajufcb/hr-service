package com.emis.hrservice.dto.request;

public record AuditContext(
        Long userId,
        String role,
        String source
) {}
