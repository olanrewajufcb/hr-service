package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffSubjectSpecialization;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StaffSubjectSpecializationRepository extends ReactiveCrudRepository<StaffSubjectSpecialization, Long> {

    Flux<StaffSubjectSpecialization> findByStaffId(Long staffId);
    
    Flux<StaffSubjectSpecialization> findByStaffIdAndIsDeletedFalse(Long staffId);
    
    Flux<StaffSubjectSpecialization> findByStaffIdAndSubjectCodeAndIsDeletedFalse(Long staffId, String subjectCode);
    
    @Query("UPDATE hr_schema.staff_subject_specializations SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE specialization_id = $1")
    Mono<Void> softDelete(Long specializationId);
}