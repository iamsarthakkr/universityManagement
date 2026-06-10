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
export type StudentRegistrationData = StudentRegistrationRequest; // alias

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
export type StudentRegistrationItem = StudentRegistrationResponse; // alias

export type InstructorRegistrationRequest = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    department: string;
};
export type InstructorRegistrationData = InstructorRegistrationRequest; // alias

export type InstructorRegistrationResponse = {
    id: number;
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    department: string;
    status: RegistrationStatus;
    submittedAt: string;
};
export type InstructorRegistrationItem = InstructorRegistrationResponse; // alias
