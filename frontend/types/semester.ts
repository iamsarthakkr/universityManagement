export enum SemesterTerm {
    SUMMER = 'SUMMER',
    WINTER = 'WINTER',
}

export enum SemesterStatus {
    PLANNED = 'PLANNED',
    ACTIVE = 'ACTIVE',
    COMPLETED = 'COMPLETED',
    CANCELLED = 'CANCELLED',
}

// Dates are ISO `YYYY-MM-DD` strings (server `LocalDate`).
export type Semester = {
    id: number;
    term: SemesterTerm;
    year: number;
    status: SemesterStatus;
    // Statuses the server will accept next; the UI offers only these.
    allowedTransitions: SemesterStatus[];
    registrationStartDate: string;
    registrationEndDate: string;
    startDate: string;
    endDate: string;
};

export type CreateSemesterRequest = {
    term: SemesterTerm;
    year: number;
    registrationStartDate: string;
    registrationEndDate: string;
    startDate: string;
    endDate: string;
};

export type SemesterTransitionRequest = {
    semesterId: number;
    status: SemesterStatus;
};
