export type CourseRequest = {
    departmentId: number;
    code: string;
    title: string;
    description: string;
    credits: number;
};

export type CourseResponse = {
    courseId: number;
    departmentId: number;
    code: string;
    title: string;
    description: string;
    credits: number;
};

export type CourseCatalogueGroup = {
    departmentId: number;
    departmentName: string;
    courseList: CourseResponse[];
};
