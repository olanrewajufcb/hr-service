package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffAssignment;
import com.emis.hrservice.enums.AssignmentStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StaffAssignmentRepository extends ReactiveCrudRepository<StaffAssignment, Long> {
    
    Flux<StaffAssignment> findByStaffIdAndIsDeletedFalse(Long staffId);


    @Query("""
        SELECT * FROM hr_schema.staff_assignments
        WHERE staff_id = $1
        AND class_id = $2
        AND subject_id = $3
        AND academic_year = $4
        AND is_deleted = false
    """)
    Mono<StaffAssignment> findByStaffIdAndClassIdAndSubjectIdAndAcademicYearAndIsDeletedFalse(
            Long staffId, 
            Long classId,
            Long subjectId,
            String academicYear);

}