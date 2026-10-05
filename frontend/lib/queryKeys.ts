import { EnrollmentStatus } from '@/types/enrollment';
import { RegistrationKind, RegistrationStatus } from '@/types/registration';

export const queryKeys = {
    registrations: {
        byKind: (kind: RegistrationKind) => ['registrations', kind] as const,
        list: (kind: RegistrationKind, status: RegistrationStatus) => ['registrations', kind, status] as const,
    },
    courses: {
        all: ['courses'] as const,
        catalogue: ['courses', 'catalogue'] as const,
    },
    semesters: {
        all: ['semesters'] as const,
        list: ['semesters', 'list'] as const,
        detail: (semesterId: number) => ['semesters', 'detail', semesterId] as const,
    },
    instructors: {
        all: ['instructors'] as const,
    },
    offerings: {
        // Approving, dropping or enrolling changes `enrolled` on every view of an offering, so those
        // mutations invalidate `all`.
        all: ['offerings'] as const,
        bySemester: (semesterId: number) => ['offerings', 'semester', semesterId] as const,
        detail: (offeringId: number) => ['offerings', 'detail', offeringId] as const,
        enrollments: (offeringId: number, status?: EnrollmentStatus) =>
            ['offerings', 'detail', offeringId, 'enrollments', status ?? 'ALL'] as const,
    },
    enrollments: {
        mine: ['enrollments', 'me'] as const,
    },
};
