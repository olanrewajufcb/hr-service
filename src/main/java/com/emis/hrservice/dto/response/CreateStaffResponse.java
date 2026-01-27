package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.Staff;

public record CreateStaffResponse(Long staffId,
                                  Long schoolId,
                                  String schoolCode,
                                  String schoolName,
                                  String lga,
                                  String staffCode,
                                  String fullName,
                                  String staffCategory,
                                  String staffRole,
                                  String employmentType,
                                  String status,
                                  String createdAt
    ) {
    public static CreateStaffResponse from(Staff staff) {
        return new CreateStaffResponse(
                staff.getStaffId(),
                staff.getSchoolId(),
                staff.getSchoolCode(),
                staff.getSchoolName(),
                staff.getLga(),
                staff.getStaffCode(),
                staff.getFullName(),
                staff.getStaffCategory().name(),
                staff.getStaffRole().name(),
                staff.getEmploymentType().name(),
                staff.getStatus().name(),
                staff.getCreatedAt().toString()
        );
    }
}
