package com.emis.hrservice.events.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequestedEvent {

    private Long reportId;
    private String reportType;     // STAFF_LIST
    private String reportFormat;   // PDF | XLSX
    private Long schoolId;
    private String schoolCode;
    private String academicYear;
    private String requestedBy;
}