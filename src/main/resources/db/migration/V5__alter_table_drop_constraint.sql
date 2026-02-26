DROP INDEX idx_school_attendance_policy_school_id_effective_from;


DROP INDEX idx_staff_assignments_staff_id_class_id_subject_id_academic_year;

DROP INDEX idx_staff_attendance_staff_id_attendance_date;

DROP INDEX idx_staff_subject_specializations_staff_id_subject_code;

DROP INDEX idx_staff_teaching_qualifications;

DROP INDEX idx_staff_academic_qualifications;

DROP INDEX idx_staff_code_school_code;


DROP INDEX idx_staff_id;

CREATE UNIQUE INDEX idx_school_attendance_policy_school_id_effective_from
    ON hr_schema.school_attendance_policy (school_id,  effective_from)
    WHERE is_deleted = FALSE;


CREATE UNIQUE INDEX idx_staff_assignments_staff_id_class_id_subject_id_academic_year
    ON hr_schema.staff_assignments(staff_id, class_id, subject_id, academic_year)
    WHERE is_deleted = FALSE;


CREATE UNIQUE INDEX idx_staff_attendance_staff_id_attendance_date
    ON hr_schema.staff_attendance(staff_id, attendance_date)
    Where is_deleted = false;


CREATE UNIQUE INDEX idx_staff_subject_specializations_staff_id_subject_code
    ON hr_schema.staff_subject_specializations(staff_id, subject_code)
    Where is_deleted = false;


CREATE UNIQUE INDEX idx_staff_teaching_qualifications
    ON hr_schema.staff_teaching_qualifications (staff_id,
                                                teaching_qualification, subject_of_qualification)
    where is_deleted = false;


CREATE UNIQUE INDEX idx_staff_academic_qualifications
    ON hr_schema.staff_academic_qualifications
        (staff_id, qualification_level, year_obtained)
    Where is_deleted = false;


CREATE UNIQUE INDEX idx_staff_id
    ON hr_schema.staff(staff_id)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX idx_staff_code_school_code
    ON hr_schema.staff(staff_code, school_code)
    WHERE is_deleted = FALSE;

ALTER TABLE hr_schema.school_attendance_policy

DROP column check_in_time,
DROP column cut_off_time,

ADD column check_in_time TIMESTAMPTZ NOT NULL,
ADD column cut_off_time TIMESTAMPTZ NOT NULL;

