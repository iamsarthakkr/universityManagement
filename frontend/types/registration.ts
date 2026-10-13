import type { Department } from './department';

export enum RegistrationStatus {
    PENDING = 'PENDING',
    APPROVED = 'APPROVED',
    REJECTED = 'REJECTED',
}

export enum RegistrationAction {
    APPROVE = 'APPROVE',
    REJECT = 'REJECT',
}

export type RegistrationKind = 'student' | 'instructor';

export type RegistrationFilter = {
    status?: RegistrationStatus;
    departmentId?: number;
};

type RegistrationResponseBase = {
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string | null;
    status: RegistrationStatus;
    createdAt: string;
    reviewedAt: string | null;
    department: Department;
    allowedActions: RegistrationAction[];
};

export type StudentRegistrationRequest = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    dateOfBirth: string;
    departmentId: number;
};

export type StudentRegistrationResponse = RegistrationResponseBase & {
    dateOfBirth: string;
};

export type InstructorRegistrationRequest = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
    departmentId: number;
};

export type InstructorRegistrationResponse = RegistrationResponseBase;

export type RegistrationResponse = StudentRegistrationResponse | InstructorRegistrationResponse;
