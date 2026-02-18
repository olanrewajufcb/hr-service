# hr-service
Emis HR Service

/hr/analytics/attendance/today
/hr/analytics/attendance/trend
/hr/analytics/workforce/summary
/hr/analytics/qualification/gaps
/hr/analytics/risk/alerts

C) Design Analytics API contracts

Attendance Analytics Model

🔹 DAILY SUMMARY (Most important)

CREATE MATERIALIZED VIEW hr_schema.analytics_staff_attendance_daily AS
SELECT
school_id,
attendance_date,

    COUNT(*) FILTER (WHERE attendance_status = 'PRESENT') AS present_count,
    COUNT(*) FILTER (WHERE attendance_status = 'ABSENT') AS absent_count,
    COUNT(*) FILTER (WHERE attendance_status = 'LATE') AS late_count,
    COUNT(*) FILTER (WHERE attendance_status IN (
        'SICK_LEAVE',
        'ANNUAL_LEAVE',
        'MATERNITY_LEAVE',
        'STUDY_LEAVE'
    )) AS leave_count,

    COUNT(*) AS total_staff,

    ROUND(
        (COUNT(*) FILTER (WHERE attendance_status = 'PRESENT')::numeric
        / NULLIF(COUNT(*), 0)) * 100,
        2
    ) AS attendance_rate

FROM hr_schema.staff_attendance
WHERE is_deleted = false
GROUP BY school_id, attendance_date;

 ========================================================================================================================
 CREATE TABLE hr_schema.analytics_staff_absence_streak (
 staff_id BIGINT,
 school_id BIGINT,
 consecutive_absent_days INTEGER,
 last_absent_date DATE,
 risk_level VARCHAR(20), -- LOW / MEDIUM / HIGH
 updated_at TIMESTAMPTZ DEFAULT NOW(),
 PRIMARY KEY (staff_id)
 );
 ========================================================================================================================
 CREATE MATERIALIZED VIEW hr_schema.analytics_staff_composition AS
 SELECT
 school_id,

    COUNT(*) FILTER (WHERE staff_category = 'TEACHING') AS teaching_staff,
    COUNT(*) FILTER (WHERE staff_category = 'NON_TEACHING') AS non_teaching_staff,

    COUNT(*) FILTER (WHERE staff_role = 'HEAD_TEACHER') AS head_teachers,
    COUNT(*) FILTER (WHERE staff_role = 'TEACHER') AS teachers,
    COUNT(*) FILTER (WHERE staff_role = 'CAREGIVER') AS caregivers,

    COUNT(*) AS total_staff

FROM hr_schema.staff
WHERE is_deleted = false
GROUP BY school_id;

=========================================================================================================================
CREATE MATERIALIZED VIEW hr_schema.analytics_staff_qualification_summary AS
SELECT
s.school_id,

    COUNT(DISTINCT s.staff_id) AS total_staff,
    COUNT(DISTINCT q.staff_id) AS qualified_staff,

    COUNT(DISTINCT s.staff_id) -
    COUNT(DISTINCT q.staff_id) AS unqualified_staff

FROM hr_schema.staff s
LEFT JOIN hr_schema.staff_teaching_qualifications q
ON s.staff_id = q.staff_id
AND q.is_deleted = false

WHERE s.is_deleted = false
GROUP BY s.school_id;
=========================================================================================================================
CREATE TABLE hr_schema.analytics_subject_coverage_gap (
school_id BIGINT,
subject_code VARCHAR(50),
required_teachers INTEGER,
available_teachers INTEGER,
gap INTEGER,
risk_level VARCHAR(20),
updated_at TIMESTAMPTZ DEFAULT NOW(),
PRIMARY KEY (school_id, subject_code)
);
=========================================================================================================================
CREATE MATERIALIZED VIEW hr_schema.analytics_staff_deployment AS
SELECT
school_id,
COUNT(*) AS total_staff,
COUNT(*) FILTER (WHERE staff_id IN (
SELECT DISTINCT staff_id FROM hr_schema.staff_assignments
WHERE is_deleted = false
)) AS deployed_staff,
COUNT(*) FILTER (WHERE staff_id NOT IN (
SELECT DISTINCT staff_id FROM hr_schema.staff_assignments
WHERE is_deleted = false
)) AS idle_staff
FROM hr_schema.staff
WHERE is_deleted = false
GROUP BY school_id;
=========================================================================================================================
CREATE TABLE hr_schema.analytics_staff_risk_flags (
staff_id BIGINT,
school_id BIGINT,
risk_type VARCHAR(50), -- ABSENCE, QUALIFICATION, DEPLOYMENT
risk_reason TEXT,
severity VARCHAR(20), -- LOW / MEDIUM / HIGH
detected_at TIMESTAMPTZ DEFAULT NOW(),
resolved BOOLEAN DEFAULT FALSE,
PRIMARY KEY (staff_id, risk_type)
);