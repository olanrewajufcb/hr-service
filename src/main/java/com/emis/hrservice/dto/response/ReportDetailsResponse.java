package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.HrReportsConfig;

public record ReportDetailsResponse(
        Long reportId,
        String reportType,
        String status,
        String fileUrl
) {
    public static ReportDetailsResponse from(HrReportsConfig report) {
        return new ReportDetailsResponse(
                report.getReportId(),
                report.getReportType(),
                report.getGenerationStatus(),
                report.getFilePath()
        );
    }
}