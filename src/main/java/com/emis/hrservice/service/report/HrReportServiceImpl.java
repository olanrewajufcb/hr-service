package com.emis.hrservice.service.report;

import com.emis.hrservice.domain.db.HrReportsConfig;
import com.emis.hrservice.dto.request.GenerateStaffListReportRequest;
import com.emis.hrservice.dto.response.GenerateReportResponse;
import com.emis.hrservice.dto.response.ReportDetailsResponse;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.repository.HrReportsConfigRepository;
import com.emis.hrservice.service.HrReportService;
import com.emis.hrservice.service.cache.SchoolCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrReportServiceImpl implements HrReportService {

    private final HrReportsConfigRepository reportRepository;
    private final SchoolCacheService schoolCacheService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<GenerateReportResponse> generateStaffListReport(
            GenerateStaffListReportRequest request,
            String requestId
    ) {

        return schoolCacheService
                .getSchoolDetails(request.schoolCode())
                .flatMap(school -> {

                    HrReportsConfig report = HrReportsConfig.builder()
                            .schoolId(school.schoolId())
                            .schoolCode(request.schoolCode())
                            .reportType("STAFF_LIST")
                            .reportFormat(request.format().name())
                            .academicYear(request.academicYear())
                            .generationStatus("GENERATING")
                            .reportDate(LocalDate.now())
                            .reportData(toJson
                                    (request.schoolCode(),
                                    school.schoolName(),
                                            request.academicYear()))
                            .createdAt(LocalDateTime.now())
                            .build();

                    return reportRepository.save(report);
                })
                .map(saved ->
                        new GenerateReportResponse(
                                saved.getReportId(),
                                saved.getGenerationStatus(),
                                saved.getCreatedAt()
                        )
                )
                .doOnSuccess(r ->
                        log.info("[{}] Staff list report requested: {}", requestId, r.reportId()));
    }

    @Override
    public Mono<ReportDetailsResponse> getReport(Long reportId) {
        return reportRepository
                .findById(reportId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Report not found")))
                .map(ReportDetailsResponse::from);
    }

    private JsonNode toJson(String schoolCode, String schoolName, String academicYear) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("schoolCode", schoolCode);
        dataMap.put("schoolName", schoolName);
        dataMap.put("academicYear", academicYear);

        return objectMapper.valueToTree(dataMap);
    }
}