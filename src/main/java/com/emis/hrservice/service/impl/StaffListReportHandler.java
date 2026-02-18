package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.HrReportsConfig;
import com.emis.hrservice.dto.request.GenerateStaffListReportRequest;
import com.emis.hrservice.dto.response.GenerateReportResponse;
import com.emis.hrservice.dto.response.ReportDetailsResponse;
import com.emis.hrservice.events.outbox.OutboxEvent;
import com.emis.hrservice.events.outbox.OutboxEventRepository;
import com.emis.hrservice.events.outbox.ReportRequestedEvent;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.repository.HrReportsConfigRepository;
import com.emis.hrservice.service.HrReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class StaffListReportHandler implements HrReportService {

    private final HrReportsConfigRepository reportRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxRepository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<GenerateReportResponse> generateStaffListReport(
            GenerateStaffListReportRequest request,
            String requestId
    ) {

        HrReportsConfig report = HrReportsConfig.builder()
                .schoolCode(request.schoolCode())
                .reportType("STAFF_LIST")
                .reportFormat(request.format().name()) // PDF | XLSX
                .generationStatus("GENERATING")
                .academicYear(request.academicYear())
                .createdAt(LocalDateTime.now())
                .build();

        return reportRepository.save(report)
                .flatMap(saved ->
                        outboxRepository.save(
                                OutboxEvent.builder()
                                        .eventId(UUID.randomUUID())
                                        .aggregateType("REPORT")
                                        .aggregateId(saved.getReportId().toString())
                                        .eventType("REPORT_REQUESTED")
                                        .topic("hr.events.v1")
                                        .payload(objectMapper.valueToTree(
                                                ReportRequestedEvent.builder()
                                                        .reportId(saved.getReportId())
                                                        .reportType("STAFF_LIST")
                                                        .reportFormat(request.format().name())
                                                        .schoolId(saved.getSchoolId())
                                                        .schoolCode(saved.getSchoolCode())
                                                        .academicYear(saved.getAcademicYear())
//                                                        .requestedBy(request.requestedBy()) //  TODO: Get from auth
                                                        .build()
                                        ))
                                        .status("PENDING")
                                        .build()
                        ).thenReturn(
                                new GenerateReportResponse(
                                        saved.getReportId(),
                                        "GENERATING",
                                        saved.getCreatedAt()
                                )
                        )
                )
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<ReportDetailsResponse> getReport(Long reportId) {
        return reportRepository
                .findById(reportId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Report not found")))
                .map(ReportDetailsResponse::from);    }
}