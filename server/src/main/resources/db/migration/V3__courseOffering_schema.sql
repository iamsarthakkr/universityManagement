CREATE TABLE course_offering (
    id INT NOT NULL AUTO_INCREMENT,
    courseId INT NOT NULL,
    semesterId INT NOT NULL,
    instructorId INT NOT NULL,
    capacity INT NOT NULL,
    section VARCHAR(10) NOT NULL,

    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_course_offering PRIMARY KEY (id),
    CONSTRAINT fk_course_offering_course FOREIGN KEY (courseId) REFERENCES course (id),
    CONSTRAINT fk_course_offering_semester FOREIGN KEY (semesterId) REFERENCES semester (id),
    CONSTRAINT fk_course_offering_instructor FOREIGN KEY (instructorId) REFERENCES instructor (id),

    CONSTRAINT unique_course_offering UNIQUE (courseId, semesterId, section),
    CONSTRAINT chk_course_offering_capacity CHECK (capacity > 0)
);