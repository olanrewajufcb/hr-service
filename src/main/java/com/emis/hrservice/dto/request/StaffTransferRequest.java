package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.ChangeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StaffTransferRequest(
        @NotNull
        ChangeType changeType,
        @NotBlank
        String newPosition,
        @NotBlank
        String fromSchoolCode,
        @NotBlank
        String toSchoolCode,
        @NotNull
        LocalDate startDate,
        String remarks) {}
