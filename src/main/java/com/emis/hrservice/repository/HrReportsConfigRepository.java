package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.HrReportsConfig;
import com.emis.hrservice.enums.GenerationStatus;
import com.emis.hrservice.enums.ReportType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface HrReportsConfigRepository extends ReactiveCrudRepository<HrReportsConfig, Long> {

    @Query("""
       UPDATE hr_schema.hr_reports_config
       SET generation_status = $2
       WHERE report_id = $1
       """)
    Mono<Void> updateStatus(Long reportId, String status);

}