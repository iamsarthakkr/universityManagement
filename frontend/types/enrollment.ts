import type { CourseOfferingSummary } from './offering';
import type { StudentSummary } from './student';

export enum EnrollmentStatus {
    PENDING = 'PENDING',
    ENROLLED = 'ENROLLED',
    REJECTED = 'REJECTED',
    CANCELLED = 'CANCELLED',
    DROPPED = 'DROPPED',
}

export enum EnrollmentAction {
    APPROVE = 'APPROVE',
    REJECT = 'REJECT',
    CANCEL = 'CANCEL',
    DROP = 'DROP',
}

export type Enrollment = {
    id: number;
    status: EnrollmentStatus;
    student: StudentSummary;
    courseOffering: CourseOfferingSummary;
    allowedActions: EnrollmentAction[];
    createdAt: string;
    updatedAt: string;
};

export type EnrollmentFilter = {
    semesterId?: number;
    status?: EnrollmentStatus;
};

export type OfferingEnrollmentsQuery = {
    offeringId: number;
    // Omitted means every status.
    status?: EnrollmentStatus;
};

export type EnrollmentActionRequest = {
    enrollmentId: number;
    action: EnrollmentAction;
};
