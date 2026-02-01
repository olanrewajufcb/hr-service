package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.enums.StaffCategory;
import com.emis.hrservice.enums.StaffRole;
import com.emis.hrservice.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface StaffRepository extends ReactiveCrudRepository<Staff, Long> {

    Mono<Staff> findByStaffCode(String staffCode);
    

    Flux<Staff> findActiveStaffBySchoolCode(String schoolCode);
    
    Flux<Staff> findBySchoolIdAndIsDeletedFalse(Long schoolId);

    @Query("""
        SELECT * FROM hr_schema.staff s 
        WHERE s.school_code = $1 
        AND s.is_deleted = false
        ORDER BY s.created_at DESC
        LIMIT $2 OFFSET $3
    """)
    Flux<Staff> findBySchoolCodeAndIsDeletedFalse(String schoolCode, int size, long offset);
    
    Flux<Staff> findBySchoolIdAndStaffCategory(Long schoolId, StaffCategory staffCategory);
    
    Flux<Staff> findBySchoolIdAndStaffRole(Long schoolId, StaffRole staffRole);
    
    Flux<Staff> findBySchoolIdAndStatus(Long schoolId, Status status);
    
    Flux<Staff> findBySchoolIdAndStaffCategoryAndIsDeletedFalse(Long schoolId, StaffCategory staffCategory);
    
    Mono<Long> countBySchoolCodeAndIsDeletedFalse(String schoolCode);
    
    Mono<Long> countBySchoolIdAndStaffCategoryAndIsDeletedFalse(Long schoolId, StaffCategory staffCategory);
    
    Mono<Long> countBySchoolIdAndStaffRoleAndIsDeletedFalse(Long schoolId, StaffRole staffRole);
    
    @Query("""
        SELECT * FROM hr_schema.staff s 
        WHERE s.school_id = $1 
        AND ($2 IS NULL OR LOWER(s.staff_code) LIKE LOWER(CONCAT('%', $2, '%'))) 
        AND ($3 IS NULL OR LOWER(CONCAT(s.first_name, ' ', s.last_name)) LIKE LOWER(CONCAT('%', $3, '%'))) 
        AND ($4 IS NULL OR s.staff_category = $4) 
        AND ($5 IS NULL OR s.staff_role = $5) 
        AND ($6 IS NULL OR s.status = $6) 
        AND s.is_deleted = false
        ORDER BY s.created_at DESC
        LIMIT $7 OFFSET $8
    """)
    Flux<Staff> searchStaff(
            Long schoolId,
            String staffCode,
            String fullName,
            String staffCategory,
            String staffRole,
            String status,
            Integer limit,
            Integer offset);
    
    Mono<Boolean> existsByStaffCodeAndIsDeletedFalse(String staffCode);
    
    @Query("""
        SELECT * FROM hr_schema.staff s 
        WHERE s.school_id = $1 
        AND s.is_deleted = false 
        AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', $2, '%')) 
             OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', $2, '%')) 
             OR LOWER(s.staff_code) LIKE LOWER(CONCAT('%', $2, '%')))
        ORDER BY s.last_name, s.first_name
        LIMIT 50
    """)
    Flux<Staff> searchByTerm(Long schoolId, String searchTerm);
    
    @Query("UPDATE hr_schema.staff SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE staff_id = $1")
    Mono<Void> softDelete(Long staffId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM hr_schema.staff
        WHERE staff_code = $1
        AND school_id = $2
        AND is_deleted = FALSE
    )
""")
    Mono<Boolean> existsByStaffCodeAndSchoolIdAndIsDeletedFalse(String staffCode, Long schoolId);

    @Query("""
    SELECT * FROM hr_schema.staff
    WHERE staff_code = $1
    AND school_code = $2
    AND is_deleted = FALSE
""")
    Mono<Staff> findByStaffCodeAndSchoolCodeAndIsDeletedFalse(String staffCode, String schoolCode);


    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM hr_schema.staff
        WHERE staff_code = $1
        AND school_code = $2
        AND is_deleted = FALSE
    )
""")
    Mono<Boolean> existsByStaffCodeAndSchoolCodeAndIsDeletedFalse(String staffCode, String schoolCode);

  @Query("""
    UPDATE hr_schema.staff
    SET school_code = $2, school_id = $3, 
        current_school_posting_date = $4
    WHERE staff_id = $1
""")
    Mono<Integer> updateStaffSchool(Long staffId, String schoolCode, Long schoolId, LocalDate startDate);
}