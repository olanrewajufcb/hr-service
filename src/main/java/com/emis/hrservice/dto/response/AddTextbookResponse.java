package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.TextbookInventory;

public record AddTextbookResponse(
        Long textbookId) {
    public static AddTextbookResponse from(TextbookInventory textbook) {
        return new AddTextbookResponse(
                textbook.getTextbookId());
    }
}
