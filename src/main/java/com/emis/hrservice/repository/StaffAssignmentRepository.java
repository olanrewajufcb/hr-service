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

    Flux<StaffAssignment> findByStaffId(Long staffId);
    
    Flux<StaffAssignment> findByStaffIdAndIsDeletedFalse(Long staffId);
    
    Flux<StaffAssignment> findBySchoolIdAndClassIdAndIsDeletedFalse(Long schoolId, Long classId);
    
    Flux<StaffAssignment> findBySchoolIdAndSubjectIdAndIsDeletedFalse(Long schoolId, Long subjectId);
    
    Flux<StaffAssignment> findBySchoolIdAndAssignmentStatusAndIsDeletedFalse(
            Long schoolId, 
            AssignmentStatus assignmentStatus);

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
    
    @Query("""
        SELECT sa.* FROM hr_schema.staff_assignments sa
        JOIN hr_schema.staff s ON sa.staff_id = s.staff_id
        WHERE sa.school_id = $1 
        AND sa.is_deleted = false
        AND s.is_deleted = false
        AND sa.assignment_status = 'ACTIVE'
        AND ($2 IS NULL OR sa.class_id = $2)
        AND ($3 IS NULL OR sa.subject_id = $3)
        ORDER BY s.last_name, s.first_name
    """)
    Flux<StaffAssignment> findActiveAssignments(Long schoolId, Long classId, Long subjectId);
    
    @Query("UPDATE hr_schema.staff_assignments SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE assignment_id = $1")
    Mono<Void> softDelete(Long assignmentId);

    @Query("""
UPDATE hr_schema.staff_assignments
SET is_deleted = true, deleted_at = NOW()
WHERE assignment_id = (
    SELECT assignment_id
    FROM hr_schema.staff_assignments
    WHERE is_deleted = false
    ORDER BY created_at DESC
    LIMIT 1
)
""")
    Mono<Void> softDeleteLatestAssignment();
}