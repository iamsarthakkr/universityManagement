-- A rejected registration must not block a new request with the same username or email,
-- so uniqueness moves to the application: only users and PENDING registrations reserve them.
-- Accounts stay unique through the users table constraints.
ALTER TABLE student_registration
    DROP INDEX unique_student_registration_username,
    DROP INDEX unique_student_registration_email;

ALTER TABLE instructor_registration
    DROP INDEX unique_instructor_registration_username,
    DROP INDEX unique_instructor_registration_email;

CREATE INDEX idx_student_registration_username_status ON student_registration (username, registrationStatus);
CREATE INDEX idx_student_registration_email_status ON student_registration (email, registrationStatus);
CREATE INDEX idx_instructor_registration_username_status ON instructor_registration (username, registrationStatus);
CREATE INDEX idx_instructor_registration_email_status ON instructor_registration (email, registrationStatus);
