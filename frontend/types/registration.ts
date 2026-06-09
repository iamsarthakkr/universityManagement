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
    id: number;
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
    id: number;
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    department: string;
    status: RegistrationStatus;
};
