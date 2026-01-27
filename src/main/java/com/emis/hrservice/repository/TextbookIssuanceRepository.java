package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.TextbookIssuance;
import com.emis.hrservice.enums.IssuanceStatus;
import com.emis.hrservice.enums.IssuedToType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface TextbookIssuanceRepository extends ReactiveCrudRepository<TextbookIssuance, Long> {

    Flux<TextbookIssuance> findByTextbookId(Long textbookId);
    
    Flux<TextbookIssuance> findByTextbookIdAndIsDeletedFalse(Long textbookId);
    
    Flux<TextbookIssuance> findBySchoolIdAndIssuedToTypeAndIssuedToIdAndIsDeletedFalse(
            Long schoolId, 
            IssuedToType issuedToType,
            Long issuedToId);
    
    Flux<TextbookIssuance> findBySchoolIdAndIssuanceDateBetweenAndIsDeletedFalse(
            Long schoolId, 
            LocalDate startDate, 
            LocalDate endDate);
    
    Flux<TextbookIssuance> findBySchoolIdAndIssuanceStatusAndIsDeletedFalse(
            Long schoolId, 
            IssuanceStatus issuanceStatus);
    
    @Query("""
        SELECT ti.* FROM hr_schema.textbook_issuance ti
        WHERE ti.school_id = $1 
        AND ti.is_deleted = false
        AND ti.issuance_status = 'ISSUED'
        AND ti.expected_return_date < CURRENT_DATE
        ORDER BY ti.expected_return_date ASC
    """)
    Flux<TextbookIssuance> findOverdueIssuances(Long schoolId);
    
    @Query("UPDATE hr_schema.textbook_issuance SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE issuance_id = $1")
    Mono<Void> softDelete(Long issuanceId);
}