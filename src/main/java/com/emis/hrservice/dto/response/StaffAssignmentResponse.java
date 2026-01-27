package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffAssignment;

public record StaffAssignmentResponse(
        Long assignmentId,
        Long staffId,
        Long schoolId,
        Long classId,
        Long sectionId,
        String assignmentRole,
        String academicYear,
        String status
) {
    public static StaffAssignmentResponse from(StaffAssignment staffAssignment) {
        return new StaffAssignmentResponse(
                staffAssignment.getAssignmentId(),
                staffAssignment.getStaffId(),
                staffAssignment.getSchoolId(),
                staffAssignment.getClassId(),
                staffAssignment.getSectionId(),
                staffAssignment.getAssignmentRole(),
                staffAssignment.getAcademicYear(),
                staffAssignment.getAssignmentStatus()

        );
    }
}
