package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffAttendance;

public record StaffCheckInResponse(
        Long attendanceId,
        Long staffId
) {
    public static StaffCheckInResponse from(StaffAttendance staffAttendance) {
        return new StaffCheckInResponse(
                staffAttendance.getAttendanceId(),
                staffAttendance.getStaffId()
                );
    }
}
