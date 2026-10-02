import type { Department } from './department';

export enum RegistrationStatus {
    PENDING = 'PENDING',
    APPROVED = 'APPROVED',
    REJECTED = 'REJECTED',
}

export type RegistrationKind = 'student' | 'instructor';

export function parseRegistrationStatus(value: string | undefined): RegistrationStatus | null {
    const status = value?.toUpperCase();
    return Object.values(RegistrationStatus).find((candidate) => candidate === status) ?? null;
}

type RegistrationResponseBase = {
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName?: string;
    status: RegistrationStatus;
    submittedAt: string;
    department: Department;
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
