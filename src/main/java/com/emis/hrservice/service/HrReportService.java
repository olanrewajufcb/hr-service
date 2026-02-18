package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.GenerateStaffListReportRequest;
import com.emis.hrservice.dto.response.GenerateReportResponse;
import com.emis.hrservice.dto.response.ReportDetailsResponse;
import reactor.core.publisher.Mono;

public interface HrReportService {

    Mono<GenerateReportResponse> generateStaffListReport(
            GenerateStaffListReportRequest request,
            String requestId
    );

    Mono<ReportDetailsResponse> getReport(Long reportId);
}