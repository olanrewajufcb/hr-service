package com.emis.hrservice.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceAnalyticsResponse {
    private Long schoolId;
    private LocalDate attendanceDate;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Long totalStaff;
    private Integer attendanceRate;
}
