package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffAttendance;

import java.time.LocalDate;
import java.time.LocalTime;

public record StaffAttendanceResponse(
        Long attendanceId,
        Long staffId,
        LocalDate attendanceDate,
        String attendanceStatus,
        LocalTime checkInTime,
        Long recordedBy) {
    public static StaffAttendanceResponse from(StaffAttendance attendance) {
        return new StaffAttendanceResponse(
                attendance.getAttendanceId(),
                attendance.getStaffId(),
                attendance.getAttendanceDate(),
                attendance.getAttendanceStatus(),
                attendance.getCheckInTime(),
                attendance.getRecordedBy()
        );
    }
}
