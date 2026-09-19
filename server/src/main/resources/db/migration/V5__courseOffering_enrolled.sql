ALTER TABLE course_offering
ADD COLUMN enrolled INT NOT NULL DEFAULT 0;

ALTER TABLE course_offering
ADD CONSTRAINT chk_course_offering_enrolled CHECK (enrolled >= 0 AND enrolled <= capacity);
