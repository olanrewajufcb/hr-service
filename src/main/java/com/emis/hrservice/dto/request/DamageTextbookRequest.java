package com.emis.hrservice.dto.request;

public record DamageTextbookRequest(
        Integer quantity,
        String reason,
        String reportedBy
) {}
