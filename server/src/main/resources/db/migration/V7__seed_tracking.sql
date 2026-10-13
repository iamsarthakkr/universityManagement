-- Demo data seeding (see the seed package). Every row a seed run inserts is recorded here,
-- so a rollback deletes exactly those rows (plus what depends on them), even if the seed file changed since.
CREATE TABLE seed_runs (
    id INT NOT NULL AUTO_INCREMENT,
    formatVersion INT NOT NULL,
    checksum CHAR(64) NOT NULL,
    anchorDate DATE NOT NULL,
    appliedAt TIMESTAMP NOT NULL,
    rolledBackAt TIMESTAMP NULL,

    CONSTRAINT pk_seed_runs PRIMARY KEY (id)
);

CREATE TABLE seed_rows (
    seedRunId INT NOT NULL,
    tableName VARCHAR(64) NOT NULL,
    rowId INT NOT NULL,

    CONSTRAINT pk_seed_rows PRIMARY KEY (seedRunId, tableName, rowId),
    CONSTRAINT fk_seed_rows_run FOREIGN KEY (seedRunId) REFERENCES seed_runs (id)
);
