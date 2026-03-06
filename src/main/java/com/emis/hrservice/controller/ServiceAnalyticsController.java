package com.emis.hrservice.controller;

import com.emis.hrservice.dto.response.DailyAttendanceAnalyticsResponse;
import com.emis.hrservice.dto.response.PeriodicAttendanceAnalyticsResponse;
import com.emis.hrservice.security.CanAccessRestrictedResource;
import com.emis.hrservice.service.HrAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
@Slf4j
public class ServiceAnalyticsController {

    private final HrAnalyticsService analyticsService;

    @CanAccessRestrictedResource
    @GetMapping("/analytics/attendance/daily")
    @Operation(summary = "Get daily attendance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<DailyAttendanceAnalyticsResponse> getDailyAttendanceAnalytics(
            @RequestParam String schoolCode,
            @RequestParam(required = false, defaultValue = "today") LocalDate attendanceDate
    ) {
        log.info("Get daily attendance analytics for school {}", schoolCode);
        String requestId = UUID.randomUUID().toString();
        return analyticsService.getDailyAttendanceAnalytics(schoolCode, attendanceDate, requestId);
    }

    @CanAccessRestrictedResource
    @GetMapping("/analytics/attendance/risk")
    @Operation(summary = "Get periodic attendance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<PeriodicAttendanceAnalyticsResponse> getPeriodicAttendanceAnalytics(
            @RequestParam String schoolCode,
            @RequestParam(required = false, defaultValue = "30") Integer days
    ) {
        log.info("Get periodic attendance analytics for school {}", schoolCode);
        String requestId = UUID.randomUUID().toString();
        return analyticsService.getPeriodicAttendanceAnalytics(schoolCode, days, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

}
