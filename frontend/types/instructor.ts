import type { Department } from './department';

export type Instructor = {
    id: number;
    firstName: string;
    lastName: string | null;
    email: string;
    department: Department;
};

export type InstructorSummary = Pick<Instructor, 'id' | 'firstName' | 'lastName'>;

export type InstructorFilter = {
    departmentId?: number;
};
