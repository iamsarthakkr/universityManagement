export type CourseRequest = {
    department: string;
    code: string;
    title: string;
    description: string;
    credits: number;
    capacity: number;
    instructorId: number;
};

export type CourseResponse = {
    id: number;
    department: string;
    code: string;
    title: string;
    description: string;
    credits: number;
    capacity: number;
    instructorId: number;
};
