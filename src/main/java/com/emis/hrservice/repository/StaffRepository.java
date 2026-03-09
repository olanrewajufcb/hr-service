package com.emis.hrservice.repository;

import com.emis.hrservice.domain.StaffListReportRow;
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



    Flux<Staff> findActiveStaffBySchoolCode(String schoolCode);


    @Query("""
        SELECT * FROM hr_schema.staff s 
        WHERE s.school_code = $1 
        AND s.is_deleted = false
        ORDER BY s.created_at DESC
        LIMIT $2 OFFSET $3
    """)
    Flux<Staff> findBySchoolCodeAndIsDeletedFalse(String schoolCode, int size, long offset);

    
    Mono<Long> countBySchoolCodeAndIsDeletedFalse(String schoolCode);

    
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
    SELECT * FROM hr_schema.staff
    WHERE staff_code = $1
    AND school_code = $2
    AND is_deleted = FALSE
""")
    Mono<Staff> findByStaffCodeAndSchoolCodeAndIsDeletedFalse(String staffCode, String schoolCode);


  @Query("""
    UPDATE hr_schema.staff
    SET school_code = $2, school_id = $3, 
        current_school_posting_date = $4
    WHERE staff_id = $1
""")
    Mono<Integer> updateStaffSchool(Long staffId, String schoolCode, Long schoolId, LocalDate startDate);

    @Query("""
    SELECT
        staff_code AS staffCode,
        CONCAT(first_name, ' ', last_name) AS fullName,
        staff_role AS staffRole,
        staff_category AS staffCategory,
        employment_type AS employmentType,
        status
    FROM hr_schema.staff
    WHERE school_id = $1
      AND is_deleted = false
    ORDER BY last_name, first_name
""")
    Flux<StaffListReportRow> fetchStaffForReport(Long schoolId);
}