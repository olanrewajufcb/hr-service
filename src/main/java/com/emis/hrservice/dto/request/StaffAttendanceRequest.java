package com.emis.hrservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StaffAttendanceRequest(
        @NotBlank
        String staffCode,
        @NotBlank
        String confirmerStaffCode,
        @NotNull
        Long attendanceId,
        @NotNull
        Boolean isPhysicallyConfirmed,
        @NotNull
        LocalDate attendanceDate,
        String reason,
        String notes
    ) {}
