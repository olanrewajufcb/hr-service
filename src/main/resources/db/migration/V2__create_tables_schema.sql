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
                                 staff_category VARCHAR(20) NOT NULL,
                                 staff_role VARCHAR(30) NOT NULL,

                                     employment_type VARCHAR(20) NOT NULL,
                                    salary_source VARCHAR(50),
                                 employment_date DATE,
                                 appointment_date DATE,

    -- Teaching Specific (for teachers only)
                                 main_subject_taught VARCHAR(50), -- Reference to academic_schema.subjects
                                 secondary_subjects VARCHAR(50)[], -- Array of subject codes


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
                                 source            VARCHAR(30)        -- WEB, MOBILE, SYSTEM, BATCH

);

CREATE UNIQUE INDEX idx_staff_id
    ON hr_schema.staff(staff_id)
    WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX idx_staff_code_school_code
    ON hr_schema.staff(staff_code, school_code)
    WHERE is_deleted = FALSE;

-- 2. STAFF_ACADEMIC_QUALIFICATIONS
CREATE TABLE hr_schema.staff_academic_qualifications (
                                                         qualification_id BIGSERIAL PRIMARY KEY,
                                                         staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE CASCADE,

    -- Standardized Qualification Codes (Based on your form)
                                                         qualification_level VARCHAR(30),

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

);

CREATE UNIQUE INDEX idx_staff_academic_qualifications
    ON hr_schema.staff_academic_qualifications
        (staff_id, qualification_level, year_obtained)
    Where is_deleted = false;

-- 3. STAFF_TEACHING_QUALIFICATIONS
CREATE TABLE hr_schema.staff_teaching_qualifications (
                                                         teaching_qualification_id BIGSERIAL PRIMARY KEY,
                                                         staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE CASCADE,

                                                         teaching_qualification VARCHAR(30) NOT NULL,
                                                         subject_of_qualification VARCHAR(30),

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

);


CREATE UNIQUE INDEX idx_staff_teaching_qualifications
    ON hr_schema.staff_teaching_qualifications (staff_id,
                                                teaching_qualification, subject_of_qualification)
    where is_deleted = false;

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
                                                         proficiency_level VARCHAR(20),
                                                         is_deleted BOOLEAN DEFAULT FALSE,
                                                         deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                         updated_at TIMESTAMPTZ DEFAULT NOW(),

                                                         created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                         created_by        VARCHAR(50),       -- userId / staffCode / system
                                                         created_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                                         updated_by        VARCHAR(50),
                                                         source            VARCHAR(30)         -- WEB, MOBILE, SYSTEM, BATCH

);


CREATE UNIQUE INDEX idx_staff_subject_specializations_staff_id_subject_code
    ON hr_schema.staff_subject_specializations(staff_id, subject_code)
    Where is_deleted = false;

-- 5. STAFF_ATTENDANCE (Daily attendance tracking)
CREATE TABLE hr_schema.staff_attendance (
                                            attendance_id BIGSERIAL PRIMARY KEY,
                                            staff_id BIGINT NOT NULL REFERENCES hr_schema.staff(staff_id) ON DELETE RESTRICT,
                                            staff_code VARCHAR(50) NOT NULL, -- denormalized from staff.staff_code
                                            school_id BIGINT NOT NULL,
                                            attendance_date DATE NOT NULL DEFAULT NOW(),
                                            check_in_time TIME,
                                            check_out_time TIME,
                                            attendance_status VARCHAR(20),
                                            late_reason TEXT,
                                            notes TEXT,
                                            recorded_by BIGINT, -- Staff ID who recorded
                                            recorded_at TIMESTAMPTZ DEFAULT NOW(),
                                            absence_duration VARCHAR(20),
                                            check_in_method VARCHAR(20),
                                            is_physical_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
                                            is_deleted BOOLEAN DEFAULT FALSE,
                                            deleted_at TIMESTAMPTZ DEFAULT NULL,
                                            updated_at TIMESTAMPTZ DEFAULT NOW(),
                                            confirmed_at TIMESTAMPTZ DEFAULT NULL,
                                            confirmed_by VARCHAR,
                                            finalized_at TIMESTAMPTZ NULL,

                                            created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                            created_by        VARCHAR(50),       -- userId / staffCode / system
                                            confirmed_by_role   VARCHAR(30),       -- HEAD_TEACHER, ADMIN, SYSTEM
                                            updated_by        VARCHAR(50),
                                            source            VARCHAR(30),        -- WEB, MOBILE, SYSTEM, BATCH
                                            CONSTRAINT check_present_has_checkin CHECK (
                                                attendance_status != 'PRESENT'
                                                OR check_in_time IS NOT NULL)

);
CREATE UNIQUE INDEX idx_staff_attendance_staff_id_attendance_date
    ON hr_schema.staff_attendance(staff_id, attendance_date)
    Where is_deleted = false;

-- 6. TEXTBOOK_INVENTORY
CREATE TABLE hr_schema.textbook_inventory (
                                              textbook_id BIGSERIAL PRIMARY KEY,
                                              school_id BIGINT NOT NULL,
                                              school_code VARCHAR(50) NOT NULL,
    -- Book Type
                                              book_type VARCHAR(20) CHECK (book_type IN ('PUPIL_BOOK', 'TEACHERS_BOOK', 'CAREGIVER_MANUAL')),
                                              provided_by VARCHAR(50) CHECK (provided_by IN ('GOVERNMENT', 'SCHOOL', 'DONATION', 'OTHER')),

    -- Subject and Grade Level
                                              subject_area VARCHAR(50),

                                              grade_level VARCHAR(20),
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
                                             issued_condition VARCHAR(20),
                                             returned_condition VARCHAR(20),

    -- Issued By
                                             issued_by_staff_id BIGINT,
                                             received_by VARCHAR(255),

                                             notes TEXT,

    -- System Fields
                                             issuance_status VARCHAR(20) DEFAULT 'ISSUED',
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
                                             assignment_role VARCHAR(30),
                                             academic_year VARCHAR(10),
                                             term_id BIGINT, -- Reference to academic_schema.academic_term

    -- Schedule
                                             schedule_days VARCHAR(50), -- e.g., "MON,WED,FRI"
                                             schedule_time TIME,

    -- Status
                                             assignment_status VARCHAR(20) DEFAULT 'ACTIVE',
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


);
CREATE UNIQUE INDEX idx_staff_assignments_staff_id_class_id_subject_id_academic_year
    ON hr_schema.staff_assignments(staff_id, class_id, subject_id, academic_year)
    WHERE is_deleted = FALSE;



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
                                                 change_type VARCHAR(20),

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
                                             school_code VARCHAR(100) NOT NULL,
                                             report_type VARCHAR(50),
                                             report_format VARCHAR(20),

    -- Report Parameters
                                             academic_year VARCHAR(10),
                                             report_date DATE,
                                             generated_by BIGINT,

    -- Report Data (Stored as JSON for flexibility)
                                             report_data JSONB,

    -- Status
                                             generation_status VARCHAR(20) DEFAULT 'PENDING',

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

CREATE TABLE hr_schema.textbook_inventory_transactions (
                                                           transaction_id BIGSERIAL PRIMARY KEY,
                                                           textbook_id BIGINT NOT NULL REFERENCES hr_schema.textbook_inventory(textbook_id),

                                                           transaction_type VARCHAR(30) NOT NULL,
                                                           quantity INTEGER NOT NULL CHECK (quantity > 0),

                                                           issued_to_type VARCHAR(20),

                                                           issued_to_code VARCHAR,
                                                           issued_to_name VARCHAR(255),

                                                           reference VARCHAR(100),
                                                           notes TEXT,

                                                           performed_by VARCHAR, -- staff_code
                                                           performed_at TIMESTAMPTZ DEFAULT NOW()
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


CREATE TABLE hr_schema.school_attendance_policy (
                                                          policy_id BIGSERIAL PRIMARY KEY,

                                                          school_id BIGINT NOT NULL,
                                                          school_code VARCHAR(50) NOT NULL,

                                                          check_in_time TIME NOT NULL,
                                                          cut_off_time TIME NOT NULL,

                                                          late_threshold_minutes INT DEFAULT 0,

                                                          effective_from DATE NOT NULL,
                                                          effective_to DATE,

                                                          status VARCHAR(20) DEFAULT 'ACTIVE',
                                                          is_deleted BOOLEAN DEFAULT FALSE,
                                                          deleted_at TIMESTAMPTZ DEFAULT NULL,
                                                          created_at TIMESTAMPTZ DEFAULT NOW(),
                                                          updated_at TIMESTAMPTZ DEFAULT NOW()


);

CREATE UNIQUE INDEX idx_school_attendance_policy_school_id_effective_from
    ON hr_schema.school_attendance_policy (school_id,  effective_from)
    WHERE is_deleted = FALSE;


