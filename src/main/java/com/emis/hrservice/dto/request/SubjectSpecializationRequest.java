package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.ProficiencyLevel;

public record SubjectSpecializationRequest(
        ProficiencyLevel proficiencyLevel,
        String subjectCode,
        String subjectName,
        Boolean isMainTeachingSubject
) {}
