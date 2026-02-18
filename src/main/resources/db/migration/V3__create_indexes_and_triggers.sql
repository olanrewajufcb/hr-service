-- Create indexes for performance
CREATE INDEX idx_staff_school ON hr_schema.staff(school_id);
CREATE INDEX idx_staff_category ON hr_schema.staff(staff_category);
CREATE INDEX idx_staff_role ON hr_schema.staff(staff_role);
CREATE INDEX idx_staff_status ON hr_schema.staff(status);
CREATE INDEX idx_staff_employment ON hr_schema.staff(employment_type);
CREATE INDEX idx_qualifications_staff ON hr_schema.staff_academic_qualifications(staff_id);
CREATE INDEX idx_attendance_staff_date ON hr_schema.staff_attendance(staff_id, attendance_date);
CREATE INDEX idx_textbook_school_subject ON hr_schema.textbook_inventory(school_id, subject_area, grade_level);
CREATE INDEX idx_assignments_staff ON hr_schema.staff_assignments(staff_id);
CREATE INDEX idx_assignments_class ON hr_schema.staff_assignments(class_id);
CREATE INDEX idx_service_history_staff ON hr_schema.staff_service_history(staff_id);
CREATE INDEX idx_service_history_school ON hr_schema.staff_service_history(school_id);
CREATE INDEX idx_service_history_type ON hr_schema.staff_service_history(change_type);
CREATE INDEX idx_staff_school_category ON hr_schema.staff(school_id, staff_category);
CREATE INDEX idx_staff_school_role ON hr_schema.staff(school_id, staff_role);
CREATE INDEX idx_textbook_type_subject ON hr_schema.textbook_inventory(book_type, subject_area, grade_level);
CREATE UNIQUE INDEX ux_active_posting_per_staff ON hr_schema.staff_service_history (staff_id)
    WHERE end_date IS NULL AND is_deleted = FALSE;
CREATE UNIQUE INDEX ux_active_transfer
    ON hr_schema.staff_service_history (staff_id, to_school_code, start_date)
    WHERE change_type = 'TRANSFER' AND is_deleted = false;
CREATE UNIQUE INDEX ux_outbox_event_id
    ON hr_schema.outbox_events(event_id);
CREATE UNIQUE INDEX ux_staff_main_subject
    ON hr_schema.staff_subject_specializations (staff_id)
    WHERE is_main_teaching_subject = true AND is_deleted = false;

CREATE UNIQUE INDEX uniq_textbook_inventory
    ON hr_schema.textbook_inventory (
                                     school_id,
                                     title,
                                     subject_area,
                                     edition,
                                     grade_level
        )
    WHERE is_deleted = false;

CREATE UNIQUE INDEX IF NOT EXISTS idx_attendance_daily_school_date
    ON hr_schema.analytics_staff_attendance_daily (school_id, attendance_date);

CREATE INDEX IF NOT EXISTS idx_attendance_risk_school
    ON hr_schema.analytics_staff_attendance_risk (school_id);



-- To automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION hr_schema.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to tables that need it
CREATE TRIGGER update_staff_updated_at BEFORE UPDATE ON hr_schema.staff
    FOR EACH ROW EXECUTE FUNCTION hr_schema.update_updated_at_column();

CREATE TRIGGER update_textbook_inventory_updated_at BEFORE UPDATE ON hr_schema.textbook_inventory
    FOR EACH ROW EXECUTE FUNCTION hr_schema.update_updated_at_column();

CREATE OR REPLACE FUNCTION hr_schema.sync_current_posting_date()
RETURNS TRIGGER AS $$
BEGIN
UPDATE hr_schema.staff
SET current_school_posting_date = NEW.start_date
WHERE staff_id = NEW.staff_id
  AND NEW.end_date IS NULL;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_current_posting
    AFTER INSERT OR UPDATE ON hr_schema.staff_service_history
                        FOR EACH ROW
                        EXECUTE FUNCTION hr_schema.sync_current_posting_date();

--  Years of Experience subject to review/change Run via:
--
-- cron
--
-- Spring Scheduler
--
-- Kubernetes CronJob
--
-- DB job (if allowed)

CREATE OR REPLACE FUNCTION hr_schema.calculate_years_of_experience()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.date_of_first_appointment IS NOT NULL THEN
    NEW.years_of_experience :=
      EXTRACT(YEAR FROM AGE(CURRENT_DATE, NEW.date_of_first_appointment))::INT;
ELSE
    NEW.years_of_experience := NULL;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_years_of_experience
    BEFORE INSERT OR UPDATE OF date_of_first_appointment
                     ON hr_schema.staff
                         FOR EACH ROW
                         EXECUTE FUNCTION hr_schema.calculate_years_of_experience();

UPDATE hr_schema.staff
SET years_of_experience =
        EXTRACT(YEAR FROM AGE(CURRENT_DATE, date_of_first_appointment))::INT
WHERE date_of_first_appointment IS NOT NULL;
