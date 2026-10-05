import { describe, expect, it } from 'vitest';

import { jsonOk, mockFetch } from '@/tests/mockFetch';
import { EnrollmentStatus } from '@/types/enrollment';
import { IApi } from '@/types/IApi';
import { SemesterStatus, SemesterTerm } from '@/types/semester';
import { createApi } from './api';

type Case = {
    name: string;
    route: string;
    call: (api: IApi) => Promise<unknown>;
    query?: string;
    body?: unknown;
};

const NEW_SEMESTER = {
    term: SemesterTerm.WINTER,
    year: 2026,
    registrationStartDate: '2026-10-01',
    registrationEndDate: '2026-10-15',
    startDate: '2026-11-01',
    endDate: '2027-03-31',
};

const NEW_OFFERING = { courseId: 3, instructorId: 4, semesterId: 7, section: 'A', capacity: 30 };

const CASES: Case[] = [
    { name: 'semesters.getSemesters', route: 'GET /semesters', call: (api) => api.semesters.getSemesters() },
    { name: 'semesters.getSemester', route: 'GET /semesters/7', call: (api) => api.semesters.getSemester(7) },
    {
        name: 'semesters.createSemester',
        route: 'POST /semesters',
        call: (api) => api.semesters.createSemester(NEW_SEMESTER),
        body: NEW_SEMESTER,
    },
    {
        name: 'semesters.transitionSemester',
        route: 'PATCH /semesters/7/status',
        call: (api) => api.semesters.transitionSemester({ semesterId: 7, status: SemesterStatus.ACTIVE }),
        body: { status: 'ACTIVE' },
    },
    { name: 'instructors.getInstructors', route: 'GET /instructor', call: (api) => api.instructors.getInstructors() },
    {
        name: 'offerings.createOffering',
        route: 'POST /course-offerings',
        call: (api) => api.offerings.createOffering(NEW_OFFERING),
        body: NEW_OFFERING,
    },
    {
        name: 'offerings.getOfferings',
        route: 'GET /course-offerings',
        call: (api) => api.offerings.getOfferings(7),
        query: 'semesterId=7',
    },
    { name: 'offerings.getOffering', route: 'GET /course-offerings/9', call: (api) => api.offerings.getOffering(9) },
    {
        name: 'offerings.getEnrollments (all statuses)',
        route: 'GET /course-offerings/9/enrollments',
        call: (api) => api.offerings.getEnrollments({ offeringId: 9 }),
        query: '',
    },
    {
        name: 'offerings.getEnrollments (one status)',
        route: 'GET /course-offerings/9/enrollments',
        call: (api) => api.offerings.getEnrollments({ offeringId: 9, status: EnrollmentStatus.PENDING }),
        query: 'enrollmentStatus=PENDING',
    },
    {
        name: 'offerings.requestEnrollment',
        route: 'POST /course-offerings/9/enrollments',
        call: (api) => api.offerings.requestEnrollment(9),
    },
    {
        name: 'enrollments.getMyEnrollments',
        route: 'GET /enrollments/me',
        call: (api) => api.enrollments.getMyEnrollments(),
    },
    {
        name: 'enrollments.approveEnrollment',
        route: 'POST /enrollments/5/approve',
        call: (api) => api.enrollments.approveEnrollment(5),
    },
    {
        name: 'enrollments.rejectEnrollment',
        route: 'POST /enrollments/5/reject',
        call: (api) => api.enrollments.rejectEnrollment(5),
    },
    {
        name: 'enrollments.cancelEnrollment',
        route: 'POST /enrollments/5/cancel',
        call: (api) => api.enrollments.cancelEnrollment(5),
    },
    {
        name: 'enrollments.dropEnrollment',
        route: 'POST /enrollments/5/drop',
        call: (api) => api.enrollments.dropEnrollment(5),
    },
];

describe('api endpoints', () => {
    it.each(CASES)('$name calls $route', async ({ route, call, query, body }) => {
        const { requests } = mockFetch({ [route]: jsonOk({}) });

        const res = await call(createApi());

        expect(res).toMatchObject({ isSuccess: true });
        expect(requests).toHaveLength(1);
        expect(`${requests[0].method} ${requests[0].path}`).toBe(route);
        if (query !== undefined) {
            expect(requests[0].query).toBe(query);
        }
        expect(requests[0].body).toEqual(body);
    });
});
