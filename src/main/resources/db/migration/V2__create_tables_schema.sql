-- =============================================
-- HR Service Schema (Nigeria Context)
-- Microservices: No FKs to external services
-- =============================================

-- Schema
-- 1. STAFF (Core Staff Table)
CREATE TABLE hr_schema.staff (
                                 staff_id BIGSERIAL PRIMARY KEY,
                                 school_id BIGINT NOT NULL,
                                 school_code VARCHAR(50) NOT NULL, -- denormalized from school service
                                 school_name VARCHAR(100) NOT NULL, --  denormalized from school service
                                 lga VARCHAR(100) NOT NULL, -- denormalized from school service
                                 staff_code VARCHAR(50) UNIQUE NOT NULL,
                                 first_name VARCHAR(100) NOT NULL,
                                 last_name VARCHAR(100) NOT NULL,
                                 other_names VARCHAR(200),
                                 gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
                                 date_of_birth DATE,
                                 email VARCHAR(255),
                                 phone VARCHAR(50),
                                 address TEXT,
                                 city VARCHAR(100),
                                 state VARCHAR(100),
                                 nationality VARCHAR(100),

    -- Employment Details
                                 staff_category VARCHAR(20) NOT NULL CHECK (staff_category IN (
                                                                              'TEACHING',
                                                                              'NON_TEACHING'


                                     )),
                                 staff_role VARCHAR(30) NOT NULL CHECK (staff_role IN (
                                     -- Teaching
                                                                              'HEAD_TEACHER',
                                                                              'ASSISTANT_HEAD_TEACHER',
                                                                              'TEACHER',
                                                                              'PRE_PRIMARY_TEACHER',

                                     -- Non-teaching
                                                                              'CAREGIVER',
                                                                              'CLERK',
                                                                              'GUARD',
                                                                              'CLEANER',
                                                                              'SECURITY',
                                                                              'DRIVER',
                                                                              'OTHERS'
                                     )),
                                 CHECK (
                                     (staff_category = 'TEACHING' AND staff_role IN (
                                                                                     'HEAD_TEACHER',
                                                                                     'ASSISTANT_HEAD_TEACHER',
                                                                                     'TEACHER',
                                                                                     'PRE_PRIMARY_TEACHER'
                                         ))
                                         OR
                                     (staff_category = 'NON_TEACHING' AND staff_role IN (
                                                                                         'CAREGIVER',
                                                                                         'CLERK',
                                                                                         'GUARD',
                                                                                         'CLEANER',
                                                                                         'SECURITY',
                                                                                         'DRIVER',
                                                                                         'OTHERS'
                                         ))
                                     ),


                                     employment_type VARCHAR(20) NOT NULL CHECK (employment_type IN (
                                                                                        'FULL_TIME',
                                                                                        'PART_TIME',
                                                                                        'VOLUNTEER',
                                                                                        'NYSC',
                                                                                        'CONTRACT'
                                     )),
                                    salary_source VARCHAR(50) CHECK (salary_source IN (
                                                                                    'FEDERAL_GOVERNMENT_FTS',
                                                                                    'STATE_GOVERNMENT_SCHOOL_PAYROLL',
                                                                                    'STATE_GOVERNMENT_OTHER_SCHOOL',
                                                                                    'COMMUNITY_PTA',
                                                                                    'VOLUNTEER_NO_SALARY',
                                                                                    'NYSC',
                                                                                    'OTHER'
                                     )),
                                 employment_date DATE,
                                 appointment_date DATE,

    -- Teaching Specific (for teachers only)
                                 main_subject_taught VARCHAR(50), -- Reference to academic_schema.subjects
                                 secondary_subjects VARCHAR(50)[], -- Array of subject codes
                                 CHECK (
                                     (staff_category = 'NON_TEACHING' AND main_subject_taught IS NULL)
                                         OR
                                     (staff_category = 'TEACHING' AND main_subject_taught IS NOT NULL)
                                     ),

                                     current_school_posting_date DATE, --You populate current_school_posting_date from the latest
   -- staff_service_history.

-- Caregiver Specific
                                 caregiver_type VARCHAR(20) CHECK (caregiver_type IN ('PRE_PRIMARY', 'PRIMARY', 'BOTH')),
                                 CHECK (
                                     caregiver_type IS NULL
                                         OR
                                     (staff_role = 'CAREGIVER' AND caregiver_type IS NOT NULL)
                                     ),

                                 staff_photo_url TEXT,
                                 trcn_number VARCHAR(50), -- Teacher Registration Council of Nigeria number
                                 date_of_first_appointment DATE,
                                 years_of_experience INTEGER, -- Calculated or stored
                                 emergency_contact_name VARCHAR(100),
                                 emergency_contact_phone VARCHAR(50),
                                 emergency_contact_relationship VARCHAR(50),
                                     -- System Fields
                                 status VARCHAR(20) DEFAULT 'ACTIVE',
                                 is_deleted BOOLEAN DEFAULT FALSE,
                                 deleted_at TIMESTAMPTZ DEFAULT NULL,
                                 updated_at TIMESTAMPTZ DEFAULT NOW(),

                                 created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 created_by        VARCHAR(50),       -- userId / staffCode / system
                                 created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                 updated_by        VARCHAR(50),
                                 source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH

                                UNIQUE (staff_code, school_code, is_deleted)


);

-- 2. STAFF_ACADEMIC_QUALIFICATIONS
CREATE TABLE hr_schema.staff_academic_qualifications (
                                                         qualification_id BIGSERIAL PRIMARY KEY,
                                                         staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE CASCADE,

    -- Standardized Qualification Codes (Based on your form)
                                                         qualification_level VARCHAR(30) CHECK (qualification_level IN (
                                                                                                                        'BELOW_SSCE',
                                                                                                                        'SSCE_WASC',
                                                                                                                        'OND_DIPLOMA',
                                                                                                                        'NCE',
                                                                                                                        'DEGREE_HND_GRADUATE',
                                                                                                                        'PGDE',
                                                                                                                        'B_ED',
                                                                                                                        'M_ED',
                                                                                                                        'GRADE_II',
                                                                                                                        'BA_ED',
                                                                                                                        'BSC_HND',
                                                                                                                        'BSC_ED',
                                                                                                                        'PHD_MASTERS',
                                                                                                                        'OTHER_DEGREE_GRADUATE'
                                                             )),

                                                         qualification_name VARCHAR(200) NOT NULL,
                                                         institution VARCHAR(255),
                                                         year_obtained INTEGER,
                                                         subject_area VARCHAR(100), -- e.g., "English", "Mathematics"

                                                         is_deleted BOOLEAN DEFAULT FALSE,
                                                         deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                         updated_at TIMESTAMPTZ DEFAULT NOW(),
                                                         created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),

                                                         created_by        VARCHAR(50),       -- userId / staffCode / system
                                                         created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                                         updated_by        VARCHAR(50),
                                                         source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH


                                                        UNIQUE(staff_id, qualification_level, year_obtained, deleted_at)
);

-- 3. STAFF_TEACHING_QUALIFICATIONS
CREATE TABLE hr_schema.staff_teaching_qualifications (
                                                         teaching_qualification_id BIGSERIAL PRIMARY KEY,
                                                         staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE CASCADE,

                                                         teaching_qualification VARCHAR(30) NOT NULL CHECK (teaching_qualification IN (
                                                                                                                              'NCE',
                                                                                                                              'PGDE',
                                                                                                                              'B_ED_EQUIVALENT',
                                                                                                                              'M_ED_EQUIVALENT',
                                                                                                                              'GRADE_I_EQUIVALENT',
                                                                                                                              'GRADE_II_EQUIVALENT',
                                                                                                                              'NONE'
                                                             )),
                                                         subject_of_qualification VARCHAR(30) CHECK (subject_of_qualification IN (
                                                                                                                                  'GENERAL_PRIMARY',
                                                                                                                                  'ENGLISH',
                                                                                                                                  'MATHEMATICS',
                                                                                                                                  'SOCIAL_STUDIES',
                                                                                                                                  'BASIC_SCIENCE',
                                                                                                                                  'HAUSA_IGBO_YORUBA',
                                                                                                                                  'CARE_GIVING',
                                                                                                                                  'OTHER',
                                                                                                                                  'NONE'
                                                             )),

                                                         institution VARCHAR(255),
                                                         year_obtained INTEGER,
                                                         certification_number VARCHAR(100),

                                                         is_deleted BOOLEAN DEFAULT FALSE,
                                                         deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                         updated_at TIMESTAMPTZ DEFAULT NOW(),
                                                         created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                         created_by        VARCHAR(50),       -- userId / staffCode / system
                                                         created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                                         updated_by        VARCHAR(50),
                                                         source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH

                                                        UNIQUE(staff_id, teaching_qualification, subject_of_qualification, deleted_at)
);

-- 4. STAFF_SUBJECT_SPECIALIZATIONS
CREATE TABLE hr_schema.staff_subject_specializations (
                                                         specialization_id BIGSERIAL PRIMARY KEY,
                                                         staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE CASCADE,
                                                         subject_code VARCHAR(50) NOT NULL, -- Reference to academic_schema.subjects.subject_code
                                                         subject_name VARCHAR(100) NOT NULL,

                                                          -- From your form: subject of qualification vs main subject taught
                                                         is_qualification_subject BOOLEAN DEFAULT FALSE, -- Subject of qualification
                                                         is_main_teaching_subject BOOLEAN DEFAULT FALSE, -- Main subject taught

                                                         years_experience_subject INTEGER DEFAULT 0,
                                                         proficiency_level VARCHAR(20) CHECK (proficiency_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
                                                         is_deleted BOOLEAN DEFAULT FALSE,
                                                         deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                         updated_at TIMESTAMPTZ DEFAULT NOW(),

                                                         created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                         created_by        VARCHAR(50),       -- userId / staffCode / system
                                                         created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                                         updated_by        VARCHAR(50),
                                                         source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH


                                                             UNIQUE(staff_id, subject_code, deleted_at)
);

-- 5. STAFF_ATTENDANCE (Daily attendance tracking)
CREATE TABLE hr_schema.staff_attendance (
                                            attendance_id BIGSERIAL PRIMARY KEY,
                                            staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE RESTRICT,
                                            school_id BIGINT NOT NULL,
                                            attendance_date DATE NOT NULL DEFAULT NOW(),
                                            check_in_time TIMESTAMPTZ,
                                            check_out_time TIMESTAMPTZ,
                                            attendance_status VARCHAR(20) CHECK (attendance_status IN ('PRESENT',
                                                                                                       'ABSENT',
                                                                                                       'LATE',
                                                                                                       'SICK_LEAVE',
                                                                                                       'ANNUAL_LEAVE',
                                                                                                       'MATERNITY_LEAVE',
                                                                                                       'STUDY_LEAVE',
                                                                                                       'OTHER_LEAVE'
                                                )),

                                            late_reason TEXT,
                                            notes TEXT,
                                            recorded_by BIGINT, -- Staff ID who recorded
                                            recorded_at TIMESTAMPTZ DEFAULT NOW(),
                                            absence_duration VARCHAR(20)
                                                CHECK (absence_duration IN (
                                                                            'LESS_THAN_ONE_MONTH',
                                                                            'MORE_THAN_ONE_MONTH'
                                                    )),
                                            check_in_method VARCHAR(20),
                                            is_deleted BOOLEAN DEFAULT FALSE,
                                            deleted_at TIMESTAMPTZ DEFAULT NULL,
                                            updated_at TIMESTAMPTZ DEFAULT NOW(),
                                            confirmed_at TIMESTAMPTZ DEFAULT NULL,
                                            confirmed_by VARCHAR,
                                            finalized_at TIMESTAMPTZ NULL

                                            created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                            created_by        VARCHAR(50),       -- userId / staffCode / system
                                            confirmed_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                            updated_by        VARCHAR(50),
                                            source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH
                                            CONSTRAINT check_present_has_checkin CHECK (
                                                attendance_status != 'PRESENT'
                                                OR check_in_time IS NOT NULL),
                                            UNIQUE(staff_id, attendance_date, deleted_at)
);

-- 6. TEXTBOOK_INVENTORY
CREATE TABLE hr_schema.textbook_inventory (
                                              textbook_id BIGSERIAL PRIMARY KEY,
                                              school_id BIGINT NOT NULL,
                                              school_code VARCHAR(50) NOT NULL,
    -- Book Type
                                              book_type VARCHAR(20) CHECK (book_type IN ('PUPIL_BOOK', 'TEACHERS_BOOK', 'CAREGIVER_MANUAL')),
                                              provided_by VARCHAR(50) CHECK (provided_by IN ('GOVERNMENT', 'SCHOOL', 'DONATION', 'OTHER')),

    -- Subject and Grade Level
                                              subject_area VARCHAR(50) CHECK (subject_area IN (
                                                                                               'ENGLISH',
                                                                                               'MATHEMATICS',
                                                                                               'SOCIAL_STUDIES',
                                                                                               'BASIC_SCIENCE_TECHNOLOGY',
                                                                                               'HAUSA',
                                                                                               'IGBO',
                                                                                               'YORUBA',
                                                                                               'GENERAL'
                                                  )),

                                              grade_level VARCHAR(20) CHECK (grade_level IN (
                                                                                             'PRE_PRIMARY',
                                                                                             'PRY1',
                                                                                             'PRY2',
                                                                                             'PRY3',
                                                                                             'PRY4',
                                                                                             'PRY5',
                                                                                             'PRY6',
                                                                                             'ALL_LEVELS'
                                                  )),

    -- Book Details
                                              title VARCHAR(255) NOT NULL,
                                              author VARCHAR(255),
                                              publisher VARCHAR(255),
                                              edition VARCHAR(50) NOT NULL,
                                              isbn VARCHAR(50),
                                              publication_year INTEGER,

    -- Inventory Details
                                              total_quantity INTEGER DEFAULT 0 CHECK (total_quantity >= 0),
                                              available_quantity INTEGER DEFAULT 0 CHECK (available_quantity >= 0),
                                              issued_quantity INTEGER DEFAULT 0 CHECK (issued_quantity >= 0),
                                              damaged_quantity INTEGER DEFAULT 0 CHECK (damaged_quantity >= 0),

    -- Storage
                                              storage_location VARCHAR(255),
                                              last_audit_date DATE,

    -- System Fields
                                              status VARCHAR(20) DEFAULT 'ACTIVE',
                                              is_deleted BOOLEAN DEFAULT FALSE,
                                              deleted_at TIMESTAMPTZ DEFAULT NULL,
                                              updated_at TIMESTAMPTZ DEFAULT NOW(),
                                              created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                              created_by        VARCHAR(50),       -- userId / staffCode / system
                                              created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                              updated_by        VARCHAR(50),
                                              source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH


                                              CHECK (available_quantity + issued_quantity + damaged_quantity <= total_quantity)
);

-- 7. TEXTBOOK_ISSUANCE
CREATE TABLE hr_schema.textbook_issuance (
                                             issuance_id BIGSERIAL PRIMARY KEY,
                                             textbook_id BIGINT NOT NULL REFERENCES hr_schema.textbook_inventory(textbook_id) ON DELETE RESTRICT,
                                             school_id BIGINT NOT NULL,

    -- Issued To
                                             issued_to_type VARCHAR(20) CHECK (issued_to_type IN ('STUDENT', 'TEACHER', 'CAREGIVER', 'CLASS')),
                                             issued_to_id BIGINT, -- Could be student_id, staff_id, or class_id
                                             issued_to_name VARCHAR(255),

    -- Issuance Details
                                             quantity_issued INTEGER NOT NULL CHECK (quantity_issued > 0),
                                             issuance_date DATE NOT NULL,
                                             expected_return_date DATE,
                                             actual_return_date DATE,

    -- Condition
                                             issued_condition VARCHAR(20) CHECK (issued_condition IN ('NEW', 'GOOD', 'FAIR', 'POOR')),
                                             returned_condition VARCHAR(20) CHECK (returned_condition IN ('NEW', 'GOOD', 'FAIR', 'POOR', 'DAMAGED', 'LOST')),

    -- Issued By
                                             issued_by_staff_id BIGINT,
                                             received_by VARCHAR(255),

                                             notes TEXT,

    -- System Fields
                                             issuance_status VARCHAR(20) DEFAULT 'ISSUED' CHECK (issuance_status IN (
                                                                                                                     'ISSUED',
                                                                                                                     'RETURNED',
                                                                                                                     'OVERDUE',
                                                                                                                     'LOST',
                                                                                                                     'DAMAGED'
                                                 )),
                                             is_deleted BOOLEAN DEFAULT FALSE,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             updated_at TIMESTAMPTZ DEFAULT NOW(),

                                             created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                             created_by        VARCHAR(50),       -- userId / staffCode / system
                                             created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                             updated_by        VARCHAR(50),
                                             source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH

);

-- 8. STAFF_ASSIGNMENTS (Teaching Assignments)
CREATE TABLE hr_schema.staff_assignments (
                                             assignment_id BIGSERIAL PRIMARY KEY,
                                             staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE CASCADE,
                                             school_id BIGINT NOT NULL,

    -- Assignment Details
                                             class_id BIGINT, -- Reference to academic_schema.school_classes
                                             section_id BIGINT, -- Reference to academic_schema.class_sections
                                             subject_id BIGINT, -- Reference to academic_schema.subjects

    -- Role in this assignment
                                             assignment_role VARCHAR(30) CHECK (assignment_role IN (
                                                                                                    'FORM_TEACHER',
                                                                                                    'SUBJECT_TEACHER',
                                                                                                    'ASSISTANT_TEACHER',
                                                                                                    'CAREGIVER',
                                                                                                    'SUPERVISOR'
                                                 )),

    -- Period
                                             academic_year VARCHAR(10),
                                             term_id BIGINT, -- Reference to academic_schema.academic_term

    -- Schedule
                                             schedule_days VARCHAR(50), -- e.g., "MON,WED,FRI"
                                             schedule_time TIME,

    -- Status
                                             assignment_status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (assignment_status IN (
                                                                                                                         'ACTIVE',
                                                                                                                         'COMPLETED',
                                                                                                                         'TRANSFERRED',
                                                                                                                         'CANCELLED'
                                                 )),

                                             start_date DATE,
                                             end_date DATE,

                                             notes TEXT,

    -- System Fields
                                             is_deleted BOOLEAN DEFAULT FALSE,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             updated_at TIMESTAMPTZ DEFAULT NOW(),
                                             created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                             created_by        VARCHAR(50),       -- userId / staffCode / system
                                             created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                             updated_by        VARCHAR(50),
                                             source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH


                                            UNIQUE(staff_id, class_id, subject_id, academic_year, deleted_at)
);

-- 9. STAFF_SERVICE_HISTORY
CREATE TABLE hr_schema.staff_service_history (
                                                 history_id BIGSERIAL PRIMARY KEY,
                                                 staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE CASCADE,

    -- Service Details
                                                 school_id BIGINT NOT NULL,
                                                 position VARCHAR(100),
                                                 start_date DATE,
                                                 end_date DATE,

    -- Type of change
                                                 change_type VARCHAR(20) CHECK (change_type IN (
                                                                                                'APPOINTMENT',
                                                                                                'PROMOTION',
                                                                                                'TRANSFER',
                                                                                                'DEPLOYMENT',
                                                                                                'RESIGNATION',
                                                                                                'RETIREMENT',
                                                                                                'TERMINATION'
                                                     )),

    -- Details
                                                 previous_position VARCHAR(100),
                                                 new_position VARCHAR(100),
                                                 from_school_id BIGINT,
                                                 to_school_id BIGINT,
                                                 from_school_code VARCHAR(100),
                                                 to_school_code VARCHAR(100),

                                                 remarks TEXT,
                                                 documented_by BIGINT, -- Staff ID who documented
                                                 document_reference VARCHAR(100),

    -- System Fields
                                                 is_deleted BOOLEAN DEFAULT FALSE,
                                                 deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                 updated_at TIMESTAMPTZ DEFAULT NOW(),
                                                 created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                 created_by        VARCHAR(50),       -- userId / staffCode / system
                                                 created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                                 updated_by        VARCHAR(50),
                                                 source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH



);

-- 10. HR_REPORTS_CONFIG (For generating reports like in the forms)
CREATE TABLE hr_schema.hr_reports_config (
                                             report_id BIGSERIAL PRIMARY KEY,
                                             school_id BIGINT NOT NULL,
                                             report_type VARCHAR(50) CHECK (report_type IN (
                                                                                            'STAFF_LIST',
                                                                                            'QUALIFICATION_SUMMARY',
                                                                                            'TEXTBOOK_INVENTORY',
                                                                                            'PUPIL_TEACHER_RATIO',
                                                                                            'CAREGIVER_SUMMARY',
                                                                                            'STAFF_DEPLOYMENT',
                                                                                            'GOVERNMENT_REPORT'
                                                 )),

    -- Report Parameters
                                             academic_year VARCHAR(10),
                                             report_date DATE,
                                             generated_by BIGINT,

    -- Report Data (Stored as JSON for flexibility)
                                             report_data JSONB,

    -- Status
                                             generation_status VARCHAR(20) DEFAULT 'PENDING' CHECK (generation_status IN (
                                                                                                                          'PENDING',
                                                                                                                          'GENERATING',
                                                                                                                          'COMPLETED',
                                                                                                                          'FAILED'
                                                 )),

                                             file_path TEXT,
                                             file_size BIGINT,

    -- System Fields
                                             is_deleted BOOLEAN DEFAULT FALSE,
                                             deleted_at TIMESTAMPTZ DEFAULT NULL,
                                             updated_at TIMESTAMPTZ DEFAULT NOW(),
                                             created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                             created_by        VARCHAR(50),       -- userId / staffCode / system
                                             created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                             updated_by        VARCHAR(50),
                                             source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH

);


CREATE TABLE hr_schema.staff_attendance_audit (
                                                  audit_id BIGSERIAL PRIMARY KEY,
                                                  attendance_id BIGINT NOT NULL,
                                                  previous_status VARCHAR(20),
                                                  new_status VARCHAR(20),
                                                  changed_by BIGINT,
                                                  changed_at TIMESTAMPTZ DEFAULT NOW(),
                                                  reason TEXT
);


CREATE TABLE hr_schema.outbox_events (
                                         outbox_id BIGSERIAL PRIMARY KEY,
                                         event_id UUID NOT NULL,
                                         aggregate_type VARCHAR(50) NOT NULL,
                                         aggregate_id VARCHAR(100) NOT NULL,
                                         event_type VARCHAR(100) NOT NULL,
                                         topic VARCHAR(200) NOT NULL,
                                         payload JSONB NOT NULL,
                                         status VARCHAR(20) DEFAULT 'PENDING',
                                         retry_count INT DEFAULT 0,
                                         created_at TIMESTAMPTZ DEFAULT NOW(),
                                         published_at TIMESTAMPTZ
);


CREATE TABLE academic_schema.school_attendance_policy (
                                                          policy_id BIGSERIAL PRIMARY KEY,

                                                          school_id BIGINT NOT NULL,
                                                          school_code VARCHAR(50) NOT NULL,

                                                          check_in_time TIME NOT NULL,
                                                          cut_off_time TIME NOT NULL,

                                                          late_threshold_minutes INT DEFAULT 0,

                                                          effective_from DATE NOT NULL,
                                                          effective_to DATE,

                                                          status VARCHAR(20) DEFAULT 'ACTIVE',

                                                          created_at TIMESTAMPTZ DEFAULT NOW(),
                                                          updated_at TIMESTAMPTZ DEFAULT NOW(),

                                                          UNIQUE (school_id, effective_from)
);



