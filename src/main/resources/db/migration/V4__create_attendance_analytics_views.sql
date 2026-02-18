CREATE MATERIALIZED VIEW IF NOT EXISTS hr_schema.analytics_staff_attendance_daily AS
SELECT
    school_id,
    attendance_date,

    COUNT(*) FILTER (WHERE attendance_status = 'PRESENT') AS present_count,
    COUNT(*) FILTER (WHERE attendance_status = 'ABSENT') AS absent_count,
    COUNT(*) FILTER (WHERE attendance_status = 'LATE') AS late_count,

    COUNT(*) FILTER (
        WHERE attendance_status IN (
            'SICK_LEAVE',
            'LEAVE',
            'ANNUAL_LEAVE',
            'MATERNITY_LEAVE',
            'STUDY_LEAVE'
        )
    ) AS leave_count,

    COUNT(*) AS total_staff,

    ROUND(
            (COUNT(*) FILTER (WHERE attendance_status = 'PRESENT')::numeric
         / NULLIF(COUNT(*), 0)) * 100,
            2
    ) AS attendance_rate

FROM hr_schema.staff_attendance
WHERE is_deleted = false
GROUP BY school_id, attendance_date;


CREATE MATERIALIZED VIEW IF NOT EXISTS hr_schema.analytics_staff_attendance_risk AS
SELECT
    sa.school_id,
    sa.staff_id,
    CONCAT(s.first_name, ' ', s.last_name) AS staff_name,

    COUNT(*) FILTER (
        WHERE sa.attendance_status = 'ABSENT'
    ) AS absent_days,

    CASE
        WHEN COUNT(*) FILTER (WHERE sa.attendance_status = 'ABSENT') >= 10 THEN 'HIGH'
        WHEN COUNT(*) FILTER (WHERE sa.attendance_status = 'ABSENT') BETWEEN 5 AND 9 THEN 'MEDIUM'
        WHEN COUNT(*) FILTER (WHERE sa.attendance_status = 'ABSENT') BETWEEN 1 AND 4 THEN 'LOW'
        ELSE 'OK'
END AS risk_level

FROM hr_schema.staff_attendance sa
JOIN hr_schema.staff s ON s.staff_id = sa.staff_id
WHERE sa.attendance_date >= CURRENT_DATE - INTERVAL '30 days' AND sa.is_deleted = false
GROUP BY sa.school_id, sa.staff_id, staff_name;
