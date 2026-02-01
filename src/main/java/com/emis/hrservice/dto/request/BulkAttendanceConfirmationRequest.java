package com.emis.hrservice.dto.request;

import java.time.LocalDate;
import java.util.List;

public record BulkAttendanceConfirmationRequest(
    LocalDate attendanceDate,
    String confirmedByStaffCode,
    List<BulkStaffAttendanceConfirmation> confirmations

    ) {}
