package com.emis.hrservice.events.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DomainEvent<T> {

    private UUID eventId;
    private String eventType;
    private int eventVersion;
    private Instant occurredAt;
    private String producer;
    private String correlationId;
    private T data;
}