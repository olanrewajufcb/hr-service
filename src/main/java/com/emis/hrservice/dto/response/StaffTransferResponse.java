package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.StaffServiceHistory;

public record StaffTransferResponse(
        Long historyId,
        Long staffId) {
    public static StaffTransferResponse from(StaffServiceHistory history) {
        return new StaffTransferResponse(
                history.getHistoryId(),
                history.getStaffId()
        );
    }
}
