package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffAcademicQualification;

import java.time.LocalDateTime;

public record AddStaffAcademicQualificationResponse(
        Long qualificationId,
        Long staffId,
        String qualificationLevel,
        String qualificationName,
        String institutionName,
        String subjectArea,
        Integer yearObtained,
        LocalDateTime createdAt
        ) {
    public static AddStaffAcademicQualificationResponse from(StaffAcademicQualification staffAcademicQualification) {
        return new AddStaffAcademicQualificationResponse(
                staffAcademicQualification.getQualificationId(),
                staffAcademicQualification.getStaffId(),
                staffAcademicQualification.getQualificationLevel().name(),
                staffAcademicQualification.getQualificationName(),
                staffAcademicQualification.getInstitution(),
                staffAcademicQualification.getSubjectArea(),
                staffAcademicQualification.getYearObtained(),
                staffAcademicQualification.getCreatedAt()
        );
    }
}
