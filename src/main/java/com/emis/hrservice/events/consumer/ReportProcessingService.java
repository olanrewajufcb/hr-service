package com.emis.hrservice.events.consumer;

import com.emis.hrservice.events.outbox.DomainEvent;
import com.emis.hrservice.events.outbox.ReportRequestedEvent;
import com.emis.hrservice.repository.HrReportsConfigRepository;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.service.ReportFileStorage;
import com.emis.hrservice.service.report.StaffListExcelGenerator;
import com.emis.hrservice.service.report.StaffListPdfGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Consumer;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReportProcessingService {

    private final HrReportsConfigRepository reportRepository;
    private final StaffRepository staffRepository;
    private final StaffListPdfGenerator pdfGenerator;
    private final StaffListExcelGenerator excelGenerator;
    private final ReportFileStorage storage;
    private final ObjectMapper objectMapper;

    public Mono<Void> process(DomainEvent<JsonNode> event) {

        if (!"REPORT_REQUESTED".equals(event.getEventType())){
            return Mono.empty();
        }

        ReportRequestedEvent payload =
                objectMapper.convertValue(event.getData(), ReportRequestedEvent.class);
        return reportRepository.findById(payload.getReportId())
                .flatMap(report ->
                        staffRepository.fetchStaffForReport(payload.getSchoolId())
                                .collectList()
                                .flatMap(staff -> {

                                    byte[] file;
                                    String extension;

                                    if ("XLSX".equals(payload.getReportFormat())) {
                                        file = excelGenerator.generate(
                                                payload.getSchoolCode(),
                                                payload.getAcademicYear(),
                                                staff
                                        );
                                        extension = "xlsx";
                                    } else {
                                        file = pdfGenerator.generate(
                                                payload.getSchoolCode(),
                                                payload.getAcademicYear(),
                                                staff
                                        );
                                        extension = "pdf";
                                    }

                                    String fileName =
                                            "staff-list-%s-%s.%s"
                                                    .formatted(
                                                            payload.getSchoolCode(),
                                                            payload.getAcademicYear(),
                                                            extension
                                                    );

                                    return storage.upload(fileName, file)
                                            .flatMap(url -> {
                                                report.setFilePath(url);
                                                report.setGenerationStatus("COMPLETED");
                                                report.setUpdatedAt(LocalDateTime.now());
                                                return reportRepository.save(report);
                                            });
                                })
                ) .onErrorResume(ex -> {
                    log.error("Report generation failed", ex);
                    return reportRepository
                            .updateStatus(payload.getReportId(), "FAILED")
                            .then(Mono.empty());
                }).then();

    }
}
