package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.QualificationLevel;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "staff_academic_qualifications", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAcademicQualification {

    @Id
    private Long qualificationId;

    @Column("staff_id")
    private Long staffId;

    @Column("qualification_level")
    private QualificationLevel qualificationLevel;

    @Column("qualification_name")
    private String qualificationName;

    @Column("institution")
    private String institution;

    @Column("year_obtained")
    private Integer yearObtained;

    @Column("subject_area")
    private String subjectArea;

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