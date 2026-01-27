package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.GenerationStatus;
import com.emis.hrservice.enums.ReportType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "hr_reports_config", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrReportsConfig {

    @Id
    private Long reportId;

    @Column("school_id")
    private Long schoolId;

    @Column("report_type")
    private ReportType reportType;

    @Column("academic_year")
    private String academicYear;

    @Column("report_date")
    private LocalDate reportDate;

    @Column("generated_by")
    private Long generatedBy;

    @Column("report_data")
    private JsonNode reportData; // Using JsonNode for JSONB in R2DBC

    @Column("generation_status")
    private GenerationStatus generationStatus;

    @Column("file_path")
    private String filePath;

    @Column("file_size")
    private Long fileSize;

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