package com.emis.hrservice.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffListReportRow {
    private String staffCode;
    private String fullName;
    private String staffRole;
    private String staffCategory;
    private String employmentType;
    private String status;
}
