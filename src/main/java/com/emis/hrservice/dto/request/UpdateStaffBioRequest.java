package com.emis.hrservice.dto.request;

import java.time.LocalDate;

public record UpdateStaffBioRequest(
        LocalDate dateOfBirth,
        String schoolCode,
        String email,
        String phoneNumber,
        String address,
        String nationality) {}
