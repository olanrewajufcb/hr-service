package com.emis.hrservice.domain.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("school_attendance_policy")
public class AttendancePolicy {
    @Id
    Long policyId;
    String schoolCode;
    Long schoolId;
    LocalTime checkInTime;
    String status;
    LocalTime cutOffTime;
    LocalDate effectiveFrom;
    LocalDate effectiveTo;
}