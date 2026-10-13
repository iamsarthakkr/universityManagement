import type { Department } from './department';

export type Student = {
    id: number;
    firstName: string;
    lastName: string | null;
    email: string;
    department: Department;
};

export type StudentSummary = Pick<Student, 'id' | 'firstName' | 'lastName'>;

export type StudentFilter = {
    departmentId?: number;
};
