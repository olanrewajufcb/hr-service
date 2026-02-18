package com.emis.hrservice.events.consumer;

import com.emis.hrservice.events.outbox.DomainEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class ReportConsumer {

    private final ReportProcessingService reportService;
    public ReportConsumer(ReportProcessingService reportService) {
        this.reportService = reportService;
    }
    @Bean
    public Consumer<DomainEvent<JsonNode>> reportProcessor() {
        return event -> reportService.process(event).subscribe();

    }
}
