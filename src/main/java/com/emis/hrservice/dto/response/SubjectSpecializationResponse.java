package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffSubjectSpecialization;

public record SubjectSpecializationResponse(
        Long specializationId,
        Long staffId,
        String subjectCode,
        String subjectName,
        String proficiencyLevel) {
    public static SubjectSpecializationResponse from(StaffSubjectSpecialization  specialization) {
        return new SubjectSpecializationResponse(
                specialization.getSpecializationId(),
                specialization.getStaffId(),
                specialization.getSubjectCode(),
                specialization.getSubjectName(),
                specialization.getProficiencyLevel()
        );
    }
}
