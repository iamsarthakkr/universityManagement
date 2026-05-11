
CREATE TABLE IF NOT EXISTS student (
    id INT NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50),
    email VARCHAR(100) NOT NULL,
    CONSTRAINT pk_constraint PRIMARY KEY (id),
    CONSTRAINT unique_student UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS studentProfile (
    id INT NOT NULL AUTO_INCREMENT,
    student_id INT NOT NULL,
    phoneNumber VARCHAR(10) NOT NULL,
    dateOfBirth DATE NOT NULL,
    address VARCHAR(200) NOT NULL,
    fatherName VARCHAR(50),
    motherName VARCHAR(50),
    CONSTRAINT fk_constraint FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT unique_student_fk UNIQUE (student_id)
);

CREATE TABLE IF NOT EXISTS instructor (
    id INT NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50),
    email VARCHAR(100) NOT NULL,
    phoneNumber VARCHAR(10) NOT NULL,
    department VARCHAR(50) NOT NULL,
    CONSTRAINT pk_constraint PRIMARY KEY (id),
    CONSTRAINT unique_instructor UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS course (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(50) NOT NULL,
    code VARCHAR(10) NOT NULL,
    credits INT NOT NULL,
    instructor_id INT,
    CONSTRAINT pk_constraint PRIMARY KEY (id),
    CONSTRAINT valid_credits CHECK (credits > 0 AND credits <= 10),
    CONSTRAINT fk_constraint FOREIGN KEY (instructor_id) REFERENCES instructor(id) ON DELETE SET NULL
);