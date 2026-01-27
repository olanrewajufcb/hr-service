package com.emis.hrservice.events;

import com.emis.hrservice.events.outbox.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StaffAssignmentEventPublisher {

    private final StreamBridge streamBridge;

    public Mono<Void> publish(OutboxEvent outboxEvent) {
        return Mono.fromCallable(() -> {
            boolean sent =
                streamBridge.send(
                    "staffAssignment-out-0",
                    MessageBuilder
                        .withPayload(outboxEvent.getPayload())
                        .setHeader("eventId", outboxEvent.getEventId().toString())
                        .setHeader("aggregateId", outboxEvent.getAggregateId())
                        .setHeader("eventType", outboxEvent.getEventType())
                        .build()
                );

            if (!sent) {
                throw new IllegalStateException("Failed to send Kafka message");
            }

            return true;
        }).then();
    }
}