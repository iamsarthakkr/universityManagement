CREATE TABLE enrollment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    studentId INT NOT NULL,
    courseOfferingId INT NOT NULL,
    status VARCHAR(50) NOT NULL,

    CONSTRAINT fk_enrollment_student FOREIGN KEY (studentId) REFERENCES student(id),
    CONSTRAINT fk_enrollment_courseOffering FOREIGN KEY (courseOfferingId) REFERENCES course_offering(id),
    CONSTRAINT chk_enrollment_status CHECK (status IN ('ENROLLED', 'PENDING', 'REJECTED', 'CANCELLED', 'DROPPED')),
    CONSTRAINT unique_enrollment UNIQUE (studentId, courseOfferingId)
);