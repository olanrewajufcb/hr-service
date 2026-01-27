package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.AssignmentRole;
import com.emis.hrservice.enums.AssignmentStatus;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Table(name = "staff_assignments", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAssignment {

    @Id
    private Long assignmentId;

    @Column("staff_id")
    private Long staffId;

    @Column("school_id")
    private Long schoolId;

    @Column("class_id")
    private Long classId;

    @Column("section_id")
    private Long sectionId;

    @Column("subject_id")
    private Long subjectId;

    @Column("assignment_role")
    private String assignmentRole;

    @Column("academic_year")
    private String academicYear;

    @Column("term_id")
    private Long termId;

    @Column("schedule_days")
    private String scheduleDays;

    @Column("schedule_time")
    private LocalTime scheduleTime;

    @Column("assignment_status")
    private String assignmentStatus;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("notes")
    private String notes;

    @Column("is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}