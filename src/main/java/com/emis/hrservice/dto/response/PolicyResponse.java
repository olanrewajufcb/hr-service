package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.AttendancePolicy;

import java.time.LocalDate;
import java.time.LocalTime;

public record PolicyResponse(
    Long policyId,
    LocalTime checkInTime,
    String status,
    LocalTime cutOffTime,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    String schoolName
    ) {
    public static PolicyResponse from(AttendancePolicy policy, String schoolName){
        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getCheckInTime(),
                policy.getStatus(),
                policy.getCutOffTime(),
                policy.getEffectiveFrom(),
                policy.getEffectiveTo(),
                schoolName

        );
    }

    public static PolicyResponse from(AttendancePolicy policy){
    return new PolicyResponse(
        policy.getPolicyId(),
        policy.getCheckInTime(),
        policy.getStatus(),
        policy.getCutOffTime(),
        policy.getEffectiveFrom(),
        policy.getEffectiveTo(),
        policy.getSchoolCode());
    }
}
