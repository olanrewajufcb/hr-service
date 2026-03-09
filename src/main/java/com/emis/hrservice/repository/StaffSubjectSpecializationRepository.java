package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffSubjectSpecialization;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StaffSubjectSpecializationRepository extends ReactiveCrudRepository<StaffSubjectSpecialization, Long> {


    
    Mono<StaffSubjectSpecialization> findByStaffIdAndSubjectCodeAndIsDeletedFalse(Long staffId, String subjectCode);

    @Query("""
            SELECT EXISTS (SELECT 1 FROM hr_schema.staff_subject_specializations 
            WHERE staff_id = $1 
            AND is_main_teaching_subject = true AND is_deleted = false)
           """)
    Mono<Boolean> existsByStaffIdAndIsMainTeachingSubjectTrueAndIsDeletedFalse(Long staffId);
}