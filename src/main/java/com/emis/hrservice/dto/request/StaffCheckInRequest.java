package com.emis.hrservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record StaffCheckInRequest(

        @NotNull
        Long staffId,
        @NotBlank
        String staffCode,
        @NotBlank
        String schoolCode,
        @NotNull
        LocalDate attendanceDate,
        @NotNull
        LocalTime checkInTime,
        String notes
) {}
