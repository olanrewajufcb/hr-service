package com.emis.hrservice.dto.response;

import com.emis.hrservice.enums.AttendanceConfirmationOutcome;

public record AttendanceConfirmationResult(Long attendanceId,
                                           AttendanceConfirmationOutcome outcome,
                                           String reason) {
    public static AttendanceConfirmationResult confirmed(Long attendanceId) {
        return new AttendanceConfirmationResult(attendanceId, AttendanceConfirmationOutcome.CONFIRMED, null);
    }
    public static AttendanceConfirmationResult skipped(Long attendanceId, String reason) {
        return new AttendanceConfirmationResult(attendanceId, AttendanceConfirmationOutcome.SKIPPED, reason);
    }
    public static AttendanceConfirmationResult failed(Long attendanceId, String reason) {
        return new AttendanceConfirmationResult(attendanceId, AttendanceConfirmationOutcome.FAILED, reason);
    }
}
