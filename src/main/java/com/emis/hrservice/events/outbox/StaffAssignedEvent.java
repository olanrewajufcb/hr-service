package com.emis.hrservice.events.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffAssignedEvent {

    private Long assignmentId;

    private Long schoolId;
    private String schoolCode;

    private Long staffId;
    private String staffCode;
    private String staffName;

    private Long classId;
    private Long sectionId;
    private Long subjectId;

    private String academicYear;
    private String assignmentRole;
}