export enum RegistrationStatus {
    PENDING = 'PENDING',
    APPROVED = 'APPROVED',
    REJECTED = 'REJECTED',
}

export type StudentRegistrationRequest = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    dateOfBirth: string;
};

export type StudentRegistrationResponse = {
    id: string;
    username: string;
    email: string;
    firstName: string;
    lastName?: string;
    dateOfBirth: string;
    status: RegistrationStatus;
    submittedAt: string;
};

export type InstructorRegistrationRequest = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    department: string;
};

export type InstructorRegistrationResponse = {
    id: string;
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    department: string;
    status: RegistrationStatus;
};
