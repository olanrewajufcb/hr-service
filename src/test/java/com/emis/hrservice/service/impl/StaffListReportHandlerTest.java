package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.HrReportsConfig;
import com.emis.hrservice.dto.request.GenerateStaffListReportRequest;
import com.emis.hrservice.dto.response.GenerateReportResponse;
import com.emis.hrservice.dto.response.ReportDetailsResponse;
import com.emis.hrservice.enums.ReportFormat;
import com.emis.hrservice.events.outbox.OutboxEvent;
import com.emis.hrservice.events.outbox.OutboxEventRepository;
import com.emis.hrservice.events.outbox.ReportRequestedEvent;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.repository.HrReportsConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffListReportHandlerTest {

    @Mock
    private HrReportsConfigRepository reportRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private OutboxEventRepository outboxRepository;
    @Mock
    private TransactionalOperator transactionalOperator;

    private StaffListReportHandler reportHandler;

    @BeforeEach
    void setUp() {
        reportHandler = new StaffListReportHandler(
                reportRepository,
                objectMapper,
                outboxRepository,
                transactionalOperator
        );
    }

    @Test
    void generateStaffListReport_Success() {
        // Arrange
        GenerateStaffListReportRequest request = new GenerateStaffListReportRequest(
                "SCH001",
                "2023-2024",
                ReportFormat.PDF
        );
        String requestId = "req-123";

        HrReportsConfig savedReport = HrReportsConfig.builder()
                .reportId(1L)
                .schoolCode("SCH001")
                .reportType("STAFF_LIST")
                .reportFormat("PDF")
                .generationStatus("GENERATING")
                .academicYear("2023-2024")
                .createdAt(LocalDateTime.now())
                .build();

        when(reportRepository.save(any(HrReportsConfig.class))).thenReturn(Mono.just(savedReport));
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(Mono.just(new OutboxEvent()));
        when(objectMapper.valueToTree(any(ReportRequestedEvent.class))).thenReturn(mock(JsonNode.class));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(reportHandler.generateStaffListReport(request, requestId))
                .assertNext(response -> {
                    assertEquals(1L, response.reportId());
                    assertEquals("GENERATING", response.status());
                    assertNotNull(response.requestedAt());
                })
                .verifyComplete();

        verify(reportRepository).save(any(HrReportsConfig.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
        verify(transactionalOperator).transactional(any(Mono.class));
    }

    @Test
    void generateStaffListReport_VerifiesOutboxEventData() {
        // Arrange
        GenerateStaffListReportRequest request = new GenerateStaffListReportRequest(
                "SCH001",
                "2023-2024",
                ReportFormat.XLSX
        );
        String requestId = "req-123";

        HrReportsConfig savedReport = HrReportsConfig.builder()
                .reportId(100L)
                .schoolId(50L)
                .schoolCode("SCH001")
                .reportType("STAFF_LIST")
                .reportFormat("XLSX")
                .generationStatus("GENERATING")
                .academicYear("2023-2024")
                .createdAt(LocalDateTime.now())
                .build();

        when(reportRepository.save(any(HrReportsConfig.class))).thenReturn(Mono.just(savedReport));
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(Mono.just(new OutboxEvent()));
        when(objectMapper.valueToTree(any(ReportRequestedEvent.class))).thenReturn(mock(JsonNode.class));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        reportHandler.generateStaffListReport(request, requestId).block();

        // Assert
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        OutboxEvent outboxEvent = outboxCaptor.getValue();

        assertEquals("REPORT", outboxEvent.getAggregateType());
        assertEquals("100", outboxEvent.getAggregateId());
        assertEquals("REPORT_REQUESTED", outboxEvent.getEventType());
        assertEquals("hr.events.v1", outboxEvent.getTopic());
        assertEquals("PENDING", outboxEvent.getStatus());
        assertNotNull(outboxEvent.getEventId());

        ArgumentCaptor<ReportRequestedEvent> eventCaptor = ArgumentCaptor.forClass(ReportRequestedEvent.class);
        verify(objectMapper).valueToTree(eventCaptor.capture());
        ReportRequestedEvent event = eventCaptor.getValue();

        assertEquals(100L, event.getReportId());
        assertEquals("STAFF_LIST", event.getReportType());
        assertEquals("XLSX", event.getReportFormat());
        assertEquals(50L, event.getSchoolId());
        assertEquals("SCH001", event.getSchoolCode());
        assertEquals("2023-2024", event.getAcademicYear());
    }

    @Test
    void getReport_Success() {
        // Arrange
        Long reportId = 1L;
        HrReportsConfig report = HrReportsConfig.builder()
                .reportId(reportId)
                .reportType("STAFF_LIST")
                .generationStatus("COMPLETED")
                .filePath("/reports/staff-list-1.pdf")
                .build();

        when(reportRepository.findById(reportId)).thenReturn(Mono.just(report));

        // Act & Assert
        StepVerifier.create(reportHandler.getReport(reportId))
                .assertNext(response -> {
                    assertEquals(reportId, response.reportId());
                    assertEquals("STAFF_LIST", response.reportType());
                    assertEquals("COMPLETED", response.status());
                    assertEquals("/reports/staff-list-1.pdf", response.fileUrl());
                })
                .verifyComplete();
    }

    @Test
    void getReport_NotFound() {
        // Arrange
        Long reportId = 1L;
        when(reportRepository.findById(reportId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(reportHandler.getReport(reportId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
