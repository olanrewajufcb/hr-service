package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.AttendanceStatus;

public record BulkStaffAttendanceConfirmation(
        Long attendanceId,
        String staffCode,
        AttendanceStatus status,
        String notes
) {}
