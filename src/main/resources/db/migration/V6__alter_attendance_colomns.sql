
ALTER TABLE hr_schema.staff_attendance

DROP column check_in_time,
DROP column check_out_time,

ADD column check_in_time TIME,
ADD column check_out_time TIME;


ALTER TABLE hr_schema.school_attendance_policy

DROP column check_in_time,
DROP column cut_off_time,

ADD column check_in_time TIME NOT NULL,
ADD column cut_off_time TIME NOT NULL;