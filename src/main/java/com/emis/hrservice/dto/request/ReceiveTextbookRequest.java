package com.emis.hrservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReceiveTextbookRequest(
        @NotNull
        Integer quantity,
        @NotBlank
        String receivedBy,
        String reference,
        String notes) {}
