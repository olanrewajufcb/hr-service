package com.emis.hrservice.dto.response;

import java.time.LocalDateTime;

public record GenerateReportResponse(
        Long reportId,
        String status,
        LocalDateTime requestedAt
) {}