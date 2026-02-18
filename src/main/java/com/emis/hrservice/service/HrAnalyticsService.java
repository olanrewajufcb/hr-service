package com.emis.hrservice.service;

import com.emis.hrservice.dto.response.DailyAttendanceAnalyticsResponse;
import com.emis.hrservice.dto.response.PeriodicAttendanceAnalyticsResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface HrAnalyticsService {

    Mono<DailyAttendanceAnalyticsResponse> getDailyAttendanceAnalytics(
            String schoolCode, LocalDate attendanceDate,  String requestId);

    Mono<PeriodicAttendanceAnalyticsResponse> getPeriodicAttendanceAnalytics(
            String schoolCode, Integer days, String requestId);

}
