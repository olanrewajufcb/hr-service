package com.emis.hrservice.domain.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "staff_attendance_audit", schema = "hr_schema")
public class StaffAttendanceAudit {
    private Long auditId;
    private Long attendanceId;
    private String previousStatus;
    private String newStatus;
    private Long changedBy;
    private String reason;
    private LocalDate changedAt;
}
