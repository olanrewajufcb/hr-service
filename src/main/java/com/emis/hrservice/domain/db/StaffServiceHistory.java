package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.ChangeType;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "staff_service_history", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffServiceHistory {

    @Id
    private Long historyId;

    private Long staffId;

    private Long schoolId;

    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private String changeType;

    private String previousPosition;

    private String newPosition;

    @Column("from_school_id")
    private Long fromSchoolId;

    @Column("to_school_id")
    private Long toSchoolId;
    private String fromSchoolCode;
    private String toSchoolCode;
    private String remarks;

    private Long documentedBy;

    private String documentReference;

    private Boolean isDeleted;

    private LocalDateTime deletedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}