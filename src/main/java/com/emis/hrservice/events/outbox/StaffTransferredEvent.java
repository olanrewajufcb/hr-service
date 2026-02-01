package com.emis.hrservice.events.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffTransferredEvent {

    private Long historyId;

    private Long staffId;
    private String staffCode;
    private String staffName;

    private Long fromSchoolId;
    private String fromSchoolCode;

    private Long toSchoolId;
    private String toSchoolCode;

    private String newPosition;
    private String changeType; // TRANSFER

    private LocalDate startDate;
}