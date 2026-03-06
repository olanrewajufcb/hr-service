package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.GenerateStaffListReportRequest;
import com.emis.hrservice.dto.response.GenerateReportResponse;
import com.emis.hrservice.dto.response.ReportDetailsResponse;
import com.emis.hrservice.security.CanAccessRestrictedResource;
import com.emis.hrservice.service.HrReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
@Slf4j
public class HrReportController {

    private final HrReportService reportService;

    @CanAccessRestrictedResource
    @PostMapping("/reports/staff-list")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<GenerateReportResponse> generateStaffList(
            @RequestHeader(required = false) String schoolCode,
            @RequestBody @Valid GenerateStaffListReportRequest request
    ) {
        log.info("Generating staff list report for school: {}", schoolCode);
        String requestId = UUID.randomUUID().toString();
        return reportService.generateStaffListReport(request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanAccessRestrictedResource
    @GetMapping("/reports/{reportId}")
    public Mono<ReportDetailsResponse> getReport(
            @RequestHeader(required = false) String schoolCode,
            @PathVariable Long reportId
    ) {
        log.info("Retrieving report details for reportId: {} and schoolCode {}", reportId, schoolCode);
        return reportService.getReport(reportId);
    }
}