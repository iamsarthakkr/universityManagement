-- Match instructor_registration: deleting the reviewing user keeps the registration and clears its reviewer.
ALTER TABLE student_registration
    DROP FOREIGN KEY fk_student_registration_reviewedBy;

ALTER TABLE student_registration
    ADD CONSTRAINT fk_student_registration_reviewedBy FOREIGN KEY (reviewedBy) REFERENCES users (id) ON DELETE SET NULL;
