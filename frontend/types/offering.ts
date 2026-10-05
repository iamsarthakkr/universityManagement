export type CourseOffering = {
    id: number;
    courseId: number;
    instructorId: number;
    semesterId: number;
    section: string;
    capacity: number;
    enrolled: number;
};

export type CreateCourseOfferingRequest = {
    courseId: number;
    instructorId: number;
    semesterId: number;
    section: string;
    capacity: number;
};
