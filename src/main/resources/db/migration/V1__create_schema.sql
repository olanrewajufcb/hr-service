-- =============================================
-- HR Service Schema (Nigeria Context)
-- Microservices: No FKs to external services
-- =============================================

-- Schema
CREATE SCHEMA IF NOT EXISTS hr_schema AUTHORIZATION hr_user;

GRANT USAGE, CREATE ON SCHEMA hr_schema TO hr_user;

-- Default privileges
ALTER DEFAULT PRIVILEGES IN SCHEMA hr_schema
GRANT ALL ON TABLES TO hr_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA hr_schema
GRANT ALL ON SEQUENCES TO hr_user;
