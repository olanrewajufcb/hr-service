package com.emis.hrservice.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendancePolicyRequest(
    LocalTime checkInTime,
    LocalTime cutOffTime,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
    ) {}
