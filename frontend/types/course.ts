export type CourseRequest = {
    departmentId: number;
    code: string;
    title: string;
    description: string;
    credits: number;
    capacity: number;
    instructorId: number;
};

export type CourseResponse = {
    courseId: number;
    departmentId: number;
    departmentName: string;
    code: string;
    title: string;
    description: string;
    credits: number;
    capacity: number;
    instructorId: number;
    instructor: string;
};

export type CourseCatalogueGroup = {
    departmentId: number;
    departmentName: string;
    courseList: CourseResponse[];
};
