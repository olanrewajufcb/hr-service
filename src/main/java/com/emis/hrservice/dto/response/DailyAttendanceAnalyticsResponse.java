package com.emis.hrservice.dto.response;

import java.time.LocalDate;

public record DailyAttendanceAnalyticsResponse(
        Long schoolId,
        String schoolName,
        String schoolCode,
        LocalDate date,
        Long totalStaff,
        Long present,
        Long absent,
        Long late,
        Integer attendancePercentage
) {
    public static DailyAttendanceAnalyticsResponse from(
            AttendanceAnalyticsResponse attendanceAnalyticsResponse,
            SchoolDetailsResponse school

    ) {
        return new DailyAttendanceAnalyticsResponse(
                attendanceAnalyticsResponse.getSchoolId(),
                school.schoolName(),
                school.schoolCode(),
                attendanceAnalyticsResponse.getAttendanceDate(),
                attendanceAnalyticsResponse.getTotalStaff(),
                attendanceAnalyticsResponse.getPresentCount(),
                attendanceAnalyticsResponse.getAbsentCount(),
                attendanceAnalyticsResponse.getLateCount(),
                attendanceAnalyticsResponse.getAttendanceRate()
        );
    }
}
