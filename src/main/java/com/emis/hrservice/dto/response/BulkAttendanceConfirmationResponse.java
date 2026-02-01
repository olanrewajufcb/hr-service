package com.emis.hrservice.dto.response;


import com.emis.hrservice.enums.AttendanceConfirmationOutcome;

import java.util.List;

public record BulkAttendanceConfirmationResponse(
        String requestId,
        int confirmed,
        int skipped,
        int failed,
        List<AttendanceConfirmationResult> details
) {
    public static BulkAttendanceConfirmationResponse from(
            List<AttendanceConfirmationResult> results,
            String requestId
    ) {
       return new BulkAttendanceConfirmationResponse(
                requestId,
               (int) results.stream().filter(r -> r.outcome() == AttendanceConfirmationOutcome.CONFIRMED).count(),
               (int) results.stream().filter(r -> r.outcome() == AttendanceConfirmationOutcome.SKIPPED).count(),
               (int) results.stream().filter(r -> r.outcome() == AttendanceConfirmationOutcome.FAILED).count(),
                results
        );
    }
}
