package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.*;

import java.time.LocalDate;

public record CreateStaffRequest(
        String schoolCode,
        String staffCode,
        String firstName,
        String lastName,
        Gender gender,
        StaffCategory staffCategory,
        StaffRole staffRole,
        EmploymentType employmentType,
        SalarySource salarySource,
        LocalDate dateOfFirstAppointment,
        String mainSubjectTaught,
        String lga
) {}
