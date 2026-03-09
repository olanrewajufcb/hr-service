package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffTeachingQualification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StaffTeachingQualificationRepository extends ReactiveCrudRepository<StaffTeachingQualification, Long> {

    
    Flux<StaffTeachingQualification> findByStaffIdAndIsDeletedFalse(Long staffId, int size, long offset);


    Mono<StaffTeachingQualification> findByStaffIdAndTeachingQualificationAndSubjectOfQualificationAndIsDeletedFalse(
            Long staffId,
            String teachingQualification,
            String subjectOfQualification);

    Mono<Long> countByStaffIdAndIsDeletedFalse(Long staffId);

    @Query("""
            SELECT EXISTS(SELECT 1 FROM hr_schema.staff_teaching_qualifications 
            WHERE staff_id = $1 AND teaching_qualification != 'NONE' AND is_deleted = false)
            """)
    Mono<Boolean> staffHasQualification(Long staffId);
}