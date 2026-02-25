package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.QualificationLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddStaffAcademicQualificationRequest(
        @NotNull QualificationLevel qualificationLevel,
        @NotBlank String qualificationName,
        String institution,
        @NotNull @Min(1900)
        Integer yearObtained,
        String subjectArea
) {}
