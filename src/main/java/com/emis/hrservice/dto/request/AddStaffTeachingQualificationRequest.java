package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.SubjectOfQualification;
import com.emis.hrservice.enums.TeachingQualification;

public record AddStaffTeachingQualificationRequest(
        TeachingQualification teachingQualification,
        SubjectOfQualification subjectOfQualification,
        String institution,
        Integer yearObtained
) {}
