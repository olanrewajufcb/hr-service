package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffServiceHistory;

import java.time.LocalDate;

public record StaffTransferResponse(
        Long historyId,
        Long staffId,
          Long schoolId,
          String position,
          LocalDate startDate,
          LocalDate endDate,
          String changeType,
          String previousPosition,
          String newPosition,
          Long fromSchoolId,
          Long toSchoolId,
          String fromSchoolCode,
          String toSchoolCode

    ) {
    public static StaffTransferResponse from(StaffServiceHistory history) {
    return new StaffTransferResponse(
        history.getHistoryId(),
        history.getStaffId(),
        history.getSchoolId(),
        history.getPosition(),
        history.getStartDate(),
        history.getEndDate(),
        history.getChangeType(),
        history.getPreviousPosition(),
        history.getNewPosition(),
        history.getFromSchoolId(),
        history.getToSchoolId(),
        history.getFromSchoolCode(),
        history.getToSchoolCode());
    }
}
