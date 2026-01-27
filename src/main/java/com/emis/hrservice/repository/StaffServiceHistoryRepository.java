package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffServiceHistory;
import com.emis.hrservice.enums.ChangeType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface StaffServiceHistoryRepository extends ReactiveCrudRepository<StaffServiceHistory, Long> {

    Flux<StaffServiceHistory> findByStaffId(Long staffId);
    
    Flux<StaffServiceHistory> findByStaffIdAndIsDeletedFalse(Long staffId);
    
    Flux<StaffServiceHistory> findBySchoolIdAndChangeTypeAndIsDeletedFalse(
            Long schoolId, 
            ChangeType changeType);
    
    Flux<StaffServiceHistory> findByStaffIdAndStartDateBetweenAndIsDeletedFalse(
            Long staffId, 
            LocalDate startDate, 
            LocalDate endDate);
    
    @Query("""
        SELECT * FROM hr_schema.staff_service_history 
        WHERE staff_id = $1 
        AND is_deleted = false 
        ORDER BY start_date DESC 
        LIMIT 1
    """)
    Mono<StaffServiceHistory> findLatestByStaffId(Long staffId);
    
    @Query("UPDATE hr_schema.staff_service_history SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE history_id = $1")
    Mono<Void> softDelete(Long historyId);
}