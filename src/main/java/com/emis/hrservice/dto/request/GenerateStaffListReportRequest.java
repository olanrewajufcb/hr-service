package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.ReportFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateStaffListReportRequest(
        @NotBlank
        String schoolCode,
        @NotBlank
        String academicYear,
        @NotNull
        ReportFormat format
) {}