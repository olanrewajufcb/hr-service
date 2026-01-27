package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.AbsenceDuration;
import com.emis.hrservice.enums.AttendanceStatus;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "staff_attendance", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAttendance {

    @Id
    private Long attendanceId;

    @Column("staff_id")
    private Long staffId;

    @Column("school_id")
    private Long schoolId;

    @Column("attendance_date")
    private LocalDate attendanceDate;

    @Column("check_in_time")
    private LocalDateTime checkInTime;

    @Column("check_out_time")
    private LocalDateTime checkOutTime;

    @Column("attendance_status")
    private AttendanceStatus attendanceStatus;

    @Column("check_in_method")
    private String checkInMethod;

    @Column("notes")
    private String notes;

    @Column("recorded_by")
    private Long recordedBy;

    @CreatedDate
    @Column("recorded_at")
    private LocalDateTime recordedAt;

    @Column("absence_duration")
    private AbsenceDuration absenceDuration;

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