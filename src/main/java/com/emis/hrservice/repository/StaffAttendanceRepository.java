package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffAttendance;
import com.emis.hrservice.enums.AttendanceStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface StaffAttendanceRepository extends ReactiveCrudRepository<StaffAttendance, Long> {

    Flux<StaffAttendance> findByStaffId(Long staffId);
    
    Flux<StaffAttendance> findByStaffIdAndIsDeletedFalse(Long staffId);
    
    Flux<StaffAttendance> findBySchoolIdAndAttendanceDateAndIsDeletedFalse(Long schoolId, LocalDate attendanceDate);
    
    Mono<StaffAttendance> findByStaffIdAndAttendanceDateAndIsDeletedFalse(Long staffId, LocalDate attendanceDate);
    
    Flux<StaffAttendance> findByStaffIdAndAttendanceDateBetweenAndIsDeletedFalse(
            Long staffId, 
            LocalDate startDate, 
            LocalDate endDate);
    
    Flux<StaffAttendance> findBySchoolIdAndAttendanceDateBetweenAndIsDeletedFalse(
            Long schoolId, 
            LocalDate startDate, 
            LocalDate endDate);
    
    Mono<Long> countByStaffIdAndAttendanceStatusAndAttendanceDateBetweenAndIsDeletedFalse(
            Long staffId, 
            AttendanceStatus status,
            LocalDate startDate, 
            LocalDate endDate);
    
    @Query("""
        SELECT sa.* FROM hr_schema.staff_attendance sa
        JOIN hr_schema.staff s ON sa.staff_id = s.staff_id
        WHERE sa.school_id = $1 
        AND sa.attendance_date = $2
        AND sa.is_deleted = false
        AND s.is_deleted = false
        ORDER BY s.last_name, s.first_name
    """)
    Flux<StaffAttendance> findDailyAttendanceBySchool(Long schoolId, LocalDate attendanceDate);
    
    @Query("UPDATE hr_schema.staff_attendance SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE attendance_id = $1")
    Mono<Void> softDelete(Long attendanceId);

    Mono<StaffAttendance> findByStaffIdAndSchoolIdAndIsDeletedFalse(Long staffId, Long schoolId);
    Mono<StaffAttendance> findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(Long staffId, Long schoolId, LocalDate attendanceDate);
}