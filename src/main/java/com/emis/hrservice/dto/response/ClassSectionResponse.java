package com.emis.hrservice.dto.response;

public record ClassSectionResponse(
    Long sectionId,
    Long teacherId,
    String teacherName,
    String room,
    String schedule,
    Integer currentEnrollment,
    Integer maxCapacity
    ) {}
