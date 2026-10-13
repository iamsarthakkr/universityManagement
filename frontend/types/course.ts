import type { Department } from './department';

export type CourseRequest = {
    departmentId: number;
    code: string;
    title: string;
    description: string;
    credits: number;
};

export type Course = {
    id: number;
    department: Department;
    code: string;
    title: string;
    description: string;
    credits: number;
};

export type CourseSummary = Pick<Course, 'id' | 'code' | 'title' | 'credits'>;

export type CourseFilter = {
    departmentId?: number;
};
