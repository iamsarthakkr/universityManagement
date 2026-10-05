import type { CourseOffering } from './offering';

export enum EnrollmentStatus {
    PENDING = 'PENDING',
    ENROLLED = 'ENROLLED',
    REJECTED = 'REJECTED',
    CANCELLED = 'CANCELLED',
    DROPPED = 'DROPPED',
}

export type EnrolledStudent = {
    id: number;
    name: string;
};

export type Enrollment = {
    id: number;
    studentId: number;
    courseOfferingId: number;
    enrollmentStatus: EnrollmentStatus;
};

export type EnrollmentDetail = {
    id: number;
    enrollmentStatus: EnrollmentStatus;
    // Statuses the server will accept next; the UI offers only these.
    allowedTransitions: EnrollmentStatus[];
    student: EnrolledStudent;
    courseOffering: CourseOffering;
};

export type OfferingEnrollmentsQuery = {
    offeringId: number;
    // Omitted means every status.
    status?: EnrollmentStatus;
};
