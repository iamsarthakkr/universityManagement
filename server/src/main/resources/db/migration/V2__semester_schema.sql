CREATE TABLE semester (
    id INT NOT NULL AUTO_INCREMENT,
    
    term VARCHAR(10) NOT NULL,
    year INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    
    registrationStartDate DATE NOT NULL,
    registrationEndDate DATE NOT NULL,
    
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_semester PRIMARY KEY (id),
    CONSTRAINT unique_semester_term_year UNIQUE (term, year),
    CONSTRAINT chk_semester_dates CHECK (
        registrationStartDate < registrationEndDate AND startDate < endDate
    ),
    
    CONSTRAINT chk_semester_term CHECK (term IN ('SUMMER', 'WINTER')),
    CONSTRAINT chk_semester_status CHECK (status IN (
        'PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED'
    ))
);
