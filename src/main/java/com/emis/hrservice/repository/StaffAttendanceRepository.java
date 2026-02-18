package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffAttendance;
import com.emis.hrservice.dto.response.AttendanceAnalyticsResponse;
import com.emis.hrservice.dto.response.PeriodicAttendanceAnalytics;
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

    // Method to refresh the materialized view (call periodically or when needed)
    @Query("REFRESH MATERIALIZED VIEW CONCURRENTLY hr_schema.analytics_staff_attendance_daily")
    Mono<Void> refreshDailyAttendanceAnalyticsView();

    // Query method to get analytics for a specific date and school
    @Query("""
    SELECT 
        school_id as schoolId,
        attendance_date as attendanceDate,
        present_count as presentCount,
        absent_count as absentCount,
        late_count as lateCount,
        leave_count as leaveCount,
        total_staff as totalStaff,
        attendance_rate as attendanceRate
    FROM hr_schema.analytics_staff_attendance_daily
    WHERE school_id = $1 AND attendance_date = $2
    """)
    Mono<AttendanceAnalyticsResponse> getDailyAttendanceAnalytics(Long schoolId, LocalDate attendanceDate);

    // Query method to get analytics for a date range
    @Query("""
    SELECT 
        school_id as schoolId,
        attendance_date as attendanceDate,
        present_count as presentCount,
        absent_count as absentCount,
        late_count as lateCount,
        leave_count as leaveCount,
        total_staff as totalStaff,
        attendance_rate as attendanceRate
    FROM hr_schema.analytics_staff_attendance_daily
    WHERE school_id = $1 
    AND attendance_date BETWEEN $2 AND $3
    ORDER BY attendance_date
    """)
    Flux<AttendanceAnalyticsResponse> getAttendanceAnalyticsByDateRange(
            Long schoolId, LocalDate startDate, LocalDate endDate);

    // Get overall summary for a school
    @Query("""
    SELECT 
        school_id as schoolId,
        MAX(attendance_date) as attendanceDate,
        SUM(present_count) as presentCount,
        SUM(absent_count) as absentCount,
        SUM(late_count) as lateCount,
        SUM(leave_count) as leaveCount,
        SUM(total_staff) as totalStaff,
        ROUND(AVG(attendance_rate), 2) as attendanceRate
    FROM hr_schema.analytics_staff_attendance_daily
    WHERE school_id = $1
    GROUP BY school_id
    """)
    Mono<AttendanceAnalyticsResponse> getOverallAttendanceSummary(Long schoolId);

    @Query("REFRESH MATERIALIZED VIEW CONCURRENTLY hr_schema.analytics_staff_attendance_risk")
    Mono<Void> refreshAttendanceRiskView();

    @Query("""
    SELECT
        staff_id     AS staffId,
        staff_name   AS staffName,
        absent_days  AS absentDays,
        risk_level   AS riskLevel
    FROM hr_schema.analytics_staff_attendance_risk
    WHERE school_id = $1
    AND absent_days > 0
    ORDER BY absent_days DESC
""")
    Flux<PeriodicAttendanceAnalytics> getAttendanceRisk(Long schoolId);

}