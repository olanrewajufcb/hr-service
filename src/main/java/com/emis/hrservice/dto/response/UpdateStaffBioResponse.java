package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.Staff;

public record UpdateStaffBioResponse(
        Long staffId,
        String staffCode,
        String firstName,
        String lastName,
        String fullName,
        String staffCategory,
        String staffRole
) {
    public static UpdateStaffBioResponse from(Staff staff) {
        return new UpdateStaffBioResponse(
                staff.getStaffId(),
                staff.getStaffCode(),
                staff.getFirstName(),
                staff.getLastName(),
                staff.getFullName(),
                staff.getStaffCategory().name(),
                staff.getStaffRole().name()
        );
    }
}
