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

    
    Mono<StaffAttendance> findByStaffIdAndAttendanceDateAndIsDeletedFalse(Long staffId, LocalDate attendanceDate);
    
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