package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.GenerateStaffListReportRequest;
import com.emis.hrservice.dto.response.GenerateReportResponse;
import com.emis.hrservice.dto.response.ReportDetailsResponse;
import com.emis.hrservice.service.HrReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/reports")
@RequiredArgsConstructor
@Slf4j
public class HrReportController {

    private final HrReportService reportService;

    @PostMapping("/staff-list")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<GenerateReportResponse> generateStaffList(
            @RequestBody @Valid GenerateStaffListReportRequest request
    ) {
        String requestId = UUID.randomUUID().toString();
        return reportService.generateStaffListReport(request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @GetMapping("/{reportId}")
    public Mono<ReportDetailsResponse> getReport(
            @PathVariable Long reportId
    ) {
        return reportService.getReport(reportId);
    }
}