package com.emis.hrservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PeriodicAttendanceAnalytics {
    private Long schoolId;
    private Integer highRiskStaff;
    private Integer mediumRiskStaff;
    private Long staffId;
    private String staffName;
    private Integer absentDays;
    private String riskLevel;
}
