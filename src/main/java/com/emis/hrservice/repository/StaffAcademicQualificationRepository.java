package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffAcademicQualification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StaffAcademicQualificationRepository extends ReactiveCrudRepository<StaffAcademicQualification, Long> {


    @Query("""
        SELECT * FROM hr_schema.staff_academic_qualifications saq
        WHERE saq.staff_id = $1 AND saq.is_deleted = false
        ORDER BY saq.created_at DESC 
        LIMIT $2 OFFSET $3
        """)
    Flux<StaffAcademicQualification> findByStaffIdAndIsDeletedFalse(Long staffId, int size, long offset);
    
    Mono<Long> countByStaffIdAndIsDeletedFalse(Long staffId);

    @Query("""
        SELECT * FROM hr_schema.staff_academic_qualifications WHERE staff_id = $1 
         AND qualification_level = $2 AND year_obtained = $3 AND is_deleted = false
    """)
    Mono<StaffAcademicQualification> findByStaffIdAndQualificationLevelAndYearObtainedAndIsDeletedFalse(
            Long staffId, String qualificationLevel, Integer yearObtained);
}