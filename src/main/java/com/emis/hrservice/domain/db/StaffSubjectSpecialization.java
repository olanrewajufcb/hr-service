package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.ProficiencyLevel;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "staff_subject_specializations", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffSubjectSpecialization {

    @Id
    private Long specializationId;

    @Column("staff_id")
    private Long staffId;

    @Column("subject_code")
    private String subjectCode;

    @Column("subject_name")
    private String subjectName;

    @Column("is_qualification_subject")
    @Builder.Default
    private Boolean isQualificationSubject = false;

    @Column("is_main_teaching_subject")
    @Builder.Default
    private Boolean isMainTeachingSubject = false;

    @Column("years_experience_subject")
    @Builder.Default
    private Integer yearsExperienceSubject = 0;

    @Column("proficiency_level")
    private ProficiencyLevel proficiencyLevel;

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