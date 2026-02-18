package com.emis.hrservice.dto.response;

public record StaffDetails(
        Long staffId,
        String staffName,
        Integer absentDays,
        String riskLevel
) {}
