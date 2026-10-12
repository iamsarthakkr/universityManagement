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

export enum SemesterAction {
    ACTIVATE = 'ACTIVATE',
    COMPLETE = 'COMPLETE',
    CANCEL = 'CANCEL',
}

// Dates are ISO `YYYY-MM-DD` strings (server `LocalDate`).
export type Semester = {
    id: number;
    term: SemesterTerm;
    year: number;
    status: SemesterStatus;
    registrationStartDate: string;
    registrationEndDate: string;
    startDate: string;
    endDate: string;
    allowedActions: SemesterAction[];
    isRegistrationOpen: boolean;
    acceptsOfferings: boolean;
};

export type SemesterSummary = Pick<Semester, 'id' | 'term' | 'year' | 'status' | 'isRegistrationOpen'>;

export type CreateSemesterRequest = {
    term: SemesterTerm;
    year: number;
    registrationStartDate: string;
    registrationEndDate: string;
    startDate: string;
    endDate: string;
};

export type SemesterActionRequest = {
    semesterId: number;
    action: SemesterAction;
};
