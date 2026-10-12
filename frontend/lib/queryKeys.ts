import { CourseFilter } from '@/types/course';
import { EnrollmentFilter, EnrollmentStatus } from '@/types/enrollment';
import { InstructorFilter } from '@/types/instructor';
import { CourseOfferingFilter, MyCourseOfferingFilter } from '@/types/offering';
import { RegistrationKind, RegistrationStatus } from '@/types/registration';
import { StudentFilter } from '@/types/student';

export const queryKeys = {
    registrations: {
        byKind: (kind: RegistrationKind) => ['registrations', kind] as const,
        list: (kind: RegistrationKind, status: RegistrationStatus) => ['registrations', kind, status] as const,
    },
    courses: {
        all: ['courses'] as const,
        list: (filter: CourseFilter = {}) => ['courses', 'list', filter] as const,
        detail: (courseId: number) => ['courses', 'detail', courseId] as const,
    },
    semesters: {
        all: ['semesters'] as const,
        list: ['semesters', 'list'] as const,
        detail: (semesterId: number) => ['semesters', 'detail', semesterId] as const,
    },
    instructors: {
        all: ['instructors'] as const,
        list: (filter: InstructorFilter = {}) => ['instructors', 'list', filter] as const,
    },
    students: {
        all: ['students'] as const,
        list: (filter: StudentFilter = {}) => ['students', 'list', filter] as const,
    },
    offerings: {
        // Approving, dropping or enrolling changes `enrolled` on every view of an offering, so those
        // mutations invalidate `all`.
        all: ['offerings'] as const,
        list: (filter: CourseOfferingFilter = {}) => ['offerings', 'list', filter] as const,
        mine: (filter: MyCourseOfferingFilter = {}) => ['offerings', 'mine', filter] as const,
        detail: (offeringId: number) => ['offerings', 'detail', offeringId] as const,
        enrollments: (offeringId: number, status?: EnrollmentStatus) =>
            ['offerings', 'detail', offeringId, 'enrollments', status ?? 'ALL'] as const,
    },
    enrollments: {
        all: ['enrollments'] as const,
        mine: (filter: EnrollmentFilter = {}) => ['enrollments', 'mine', filter] as const,
    },
};
