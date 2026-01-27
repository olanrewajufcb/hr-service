package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.SubjectOfQualification;
import com.emis.hrservice.enums.TeachingQualification;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "staff_teaching_qualifications", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffTeachingQualification {

    @Id
    private Long teachingQualificationId;

    @Column("staff_id")
    private Long staffId;

    @Column("teaching_qualification")
    private String teachingQualification;

    @Column("subject_of_qualification")
    private String subjectOfQualification;

    @Column("institution")
    private String institution;

    @Column("year_obtained")
    private Integer yearObtained;

    @Column("certification_number")
    private String certificationNumber;

    @Column("is_deleted")
    private Boolean isDeleted;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}