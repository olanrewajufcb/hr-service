package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffTeachingQualification;

public record StaffTeachingQualificationResponse(
        Long teachingQualificationId,
        Long staffId,
        String teachingQualification,
        String institution,
        Integer yearObtained
) {
    public static StaffTeachingQualificationResponse from(StaffTeachingQualification staffTeachingQualification){
        return new StaffTeachingQualificationResponse(
                staffTeachingQualification.getTeachingQualificationId(),
                staffTeachingQualification.getStaffId(),
                staffTeachingQualification.getTeachingQualification(),
                staffTeachingQualification.getInstitution(),
                staffTeachingQualification.getYearObtained()
        );
    }
}
