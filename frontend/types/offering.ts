import type { CourseSummary } from './course';
import type { InstructorSummary } from './instructor';
import type { SemesterSummary } from './semester';

export enum OfferingAction {
    ENROLL = 'ENROLL',
}

export type CourseOffering = {
    id: number;
    course: CourseSummary;
    instructor: InstructorSummary;
    semester: SemesterSummary;
    section: string;
    capacity: number;
    enrolled: number;
    seatsAvailable: number;
    allowedActions: OfferingAction[];
};

export type CourseOfferingSummary = Pick<CourseOffering, 'id' | 'course' | 'instructor' | 'semester' | 'section'>;

export type CreateCourseOfferingRequest = {
    courseId: number;
    instructorId: number;
    semesterId: number;
    section: string;
    capacity: number;
};

export type CourseOfferingFilter = {
    semesterId?: number;
    departmentId?: number;
    instructorId?: number;
};

export type MyCourseOfferingFilter = Omit<CourseOfferingFilter, 'instructorId'>;
