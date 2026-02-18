package com.emis.hrservice.dto.response;

import java.util.List;

public record PeriodicAttendanceAnalyticsResponse(
        String schoolCode,
        Integer periodInDays,
        Integer highRiskCount,
        Integer mediumRiskCount,
        Integer lowRiskCount,
        List<PeriodicAttendanceAnalytics> staffDetails
) {
    public static PeriodicAttendanceAnalyticsResponse from(
            String schoolCode,Integer days, List<PeriodicAttendanceAnalytics> list) {
        return new PeriodicAttendanceAnalyticsResponse(
                schoolCode,
                days,
                (int) list.stream().filter(s -> "HIGH".equals(s.getRiskLevel())).count(),
                (int) list.stream().filter(s -> "MEDIUM".equals(s.getRiskLevel())).count(),
                (int) list.stream().filter(s -> "LOW".equals(s.getRiskLevel())).count(),
                list
        );
    }
}
