
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL,
    
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT unique_user_username UNIQUE (username),
    CONSTRAINT unique_user_email UNIQUE (email),
    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'STUDENT', 'INSTRUCTOR'))
);

CREATE TABLE department (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_department PRIMARY KEY (id),
    CONSTRAINT unique_department_code UNIQUE (code)
);

CREATE TABLE course (
    id INT NOT NULL AUTO_INCREMENT,
    departmentId INT NOT NULL,

    code VARCHAR(10) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    credits INT NOT NULL,

    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_course PRIMARY KEY (id),
    CONSTRAINT fk_course_department FOREIGN KEY (departmentId) REFERENCES department (id),
    CONSTRAINT unique_course_code UNIQUE (code),
    CONSTRAINT chk_course_credits CHECK (credits >= 1 AND credits < 10)
);


CREATE TABLE student (
    id INT NOT NULL AUTO_INCREMENT,
    userId INT NOT NULL,
    departmentId INT NOT NULL,

    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50),
    dateOfBirth DATE NOT NULL,
    phoneNumber VARCHAR(10),
    address VARCHAR(100),
    fatherName VARCHAR(50),
    motherName VARCHAR(50),

    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_student PRIMARY KEY (id),
    CONSTRAINT fk_student_user FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_student_department FOREIGN KEY (departmentId) REFERENCES department (id),
    CONSTRAINT unique_student_user UNIQUE (userId)
);

CREATE TABLE instructor (
    id INT NOT NULL AUTO_INCREMENT,
    userId INT NOT NULL,
    departmentId INT NOT NULL,

    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50),
    phoneNumber VARCHAR(10),
    
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_instructor PRIMARY KEY (id),
    CONSTRAINT fk_instructor_user FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_instructor_department FOREIGN KEY (departmentId) REFERENCES department (id),
    CONSTRAINT unique_instructor_user UNIQUE (userId)
);

CREATE TABLE student_registration (
    id INT NOT NULL AUTO_INCREMENT,
    departmentId INT NOT NULL,
    registrationStatus VARCHAR(20) NOT NULL,

    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50),
    dateOfBirth DATE NOT NULL,

    reviewedAt TIMESTAMP,
    reviewedBy INT,
    
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_student_registration PRIMARY KEY (id),
    CONSTRAINT fk_student_registration_department FOREIGN KEY (departmentId) REFERENCES department (id),
    CONSTRAINT chk_registration_status CHECK (registrationStatus IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT fk_student_registration_reviewedBy FOREIGN KEY (reviewedBy) REFERENCES users (id),
    CONSTRAINT unique_student_registration_username UNIQUE (username),
    CONSTRAINT unique_student_registration_email UNIQUE (email)

);

CREATE TABLE instructor_registration (
    id INT NOT NULL AUTO_INCREMENT,
    departmentId INT NOT NULL,
    registrationStatus VARCHAR(20) NOT NULL,

    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50),

    reviewedAt TIMESTAMP,
    reviewedBy INT,
    
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_instructor_registration PRIMARY KEY (id),
    CONSTRAINT fk_instructor_registration_department FOREIGN KEY (departmentId) REFERENCES department (id),
    CONSTRAINT chk_instructor_registration_status CHECK (registrationStatus IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT fk_instructor_registration_reviewedBy FOREIGN KEY (reviewedBy) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT unique_instructor_registration_username UNIQUE (username),
    CONSTRAINT unique_instructor_registration_email UNIQUE (email)
);

