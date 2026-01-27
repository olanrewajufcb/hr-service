package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.AssignmentRole;

public record CreateStaffAssignmentRequest(
        Long classId,
        Long sectionId,
        Long subjectId,
        AssignmentRole assignmentRole,
        String academicYear) {}
