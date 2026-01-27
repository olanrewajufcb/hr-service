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

    @Column("staff_id")
    private Long staffId;

    @Column("school_id")
    private Long schoolId;

    @Column("position")
    private String position;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("change_type")
    private String changeType;

    @Column("previous_position")
    private String previousPosition;

    @Column("new_position")
    private String newPosition;

    @Column("from_school_id")
    private Long fromSchoolId;

    @Column("to_school_id")
    private Long toSchoolId;

    @Column("remarks")
    private String remarks;

    @Column("documented_by")
    private Long documentedBy;

    @Column("document_reference")
    private String documentReference;

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