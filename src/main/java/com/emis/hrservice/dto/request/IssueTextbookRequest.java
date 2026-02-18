package com.emis.hrservice.dto.request;

import com.emis.hrservice.enums.IssuedToType;

public record IssueTextbookRequest(
        Integer quantity,
        IssuedToType issuedToType,
        String issuedToName,
        String issuedToCode,
        String issuedBy
        ) {}
