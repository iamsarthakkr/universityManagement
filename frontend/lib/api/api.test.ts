import { describe, expect, it } from 'vitest';

import { jsonOk, mockFetch } from '@/tests/mockFetch';
import { EnrollmentAction, EnrollmentStatus } from '@/types/enrollment';
import { IApi } from '@/types/IApi';
import { RegistrationAction, RegistrationStatus } from '@/types/registration';
import { SemesterAction, SemesterTerm } from '@/types/semester';
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

const NEW_INSTRUCTOR = {
    username: 'prof',
    password: 'secret123',
    email: 'prof@uni.edu',
    firstName: 'Kavya',
    departmentId: 1,
};

const NEW_STUDENT = { ...NEW_INSTRUCTOR, username: 'ana', dateOfBirth: '2005-02-03' };

const CASES: Case[] = [
    { name: 'auth.me', route: 'GET /me', call: (api) => api.auth.me() },
    {
        name: 'registration.createStudentRegistration',
        route: 'POST /student-registrations',
        call: (api) => api.registration.createStudentRegistration(NEW_STUDENT),
        body: NEW_STUDENT,
    },
    {
        name: 'registration.getStudentRegistrations',
        route: 'GET /student-registrations',
        call: (api) =>
            api.registration.getStudentRegistrations({ status: RegistrationStatus.PENDING, departmentId: 2 }),
        query: 'status=PENDING&departmentId=2',
    },
    {
        name: 'registration.performStudentRegistrationAction',
        route: 'POST /student-registrations/3/approve',
        call: (api) =>
            api.registration.performStudentRegistrationAction({
                registrationId: 3,
                action: RegistrationAction.APPROVE,
            }),
    },
    {
        name: 'registration.createInstructorRegistration',
        route: 'POST /instructor-registrations',
        call: (api) => api.registration.createInstructorRegistration(NEW_INSTRUCTOR),
        body: NEW_INSTRUCTOR,
    },
    {
        name: 'registration.getInstructorRegistrations',
        route: 'GET /instructor-registrations',
        call: (api) => api.registration.getInstructorRegistrations({}),
        query: '',
    },
    {
        name: 'registration.performInstructorRegistrationAction',
        route: 'POST /instructor-registrations/3/reject',
        call: (api) =>
            api.registration.performInstructorRegistrationAction({
                registrationId: 3,
                action: RegistrationAction.REJECT,
            }),
    },
    {
        name: 'courses.getCourses',
        route: 'GET /courses',
        call: (api) => api.courses.getCourses({ departmentId: 2 }),
        query: 'departmentId=2',
    },
    { name: 'courses.getCourse', route: 'GET /courses/10', call: (api) => api.courses.getCourse(10) },
    { name: 'semesters.getSemesters', route: 'GET /semesters', call: (api) => api.semesters.getSemesters() },
    { name: 'semesters.getSemester', route: 'GET /semesters/7', call: (api) => api.semesters.getSemester(7) },
    {
        name: 'semesters.createSemester',
        route: 'POST /semesters',
        call: (api) => api.semesters.createSemester(NEW_SEMESTER),
        body: NEW_SEMESTER,
    },
    {
        name: 'semesters.performAction',
        route: 'POST /semesters/7/activate',
        call: (api) => api.semesters.performAction({ semesterId: 7, action: SemesterAction.ACTIVATE }),
    },
    {
        name: 'instructors.getInstructors',
        route: 'GET /instructors',
        call: (api) => api.instructors.getInstructors({ departmentId: 1 }),
        query: 'departmentId=1',
    },
    {
        name: 'students.getStudents',
        route: 'GET /students',
        call: (api) => api.students.getStudents({}),
        query: '',
    },
    {
        name: 'offerings.createOffering',
        route: 'POST /course-offerings',
        call: (api) => api.offerings.createOffering(NEW_OFFERING),
        body: NEW_OFFERING,
    },
    {
        name: 'offerings.getOfferings',
        route: 'GET /course-offerings',
        call: (api) => api.offerings.getOfferings({ semesterId: 7, instructorId: 4 }),
        query: 'semesterId=7&instructorId=4',
    },
    {
        name: 'offerings.getMyOfferings',
        route: 'GET /me/course-offerings',
        call: (api) => api.offerings.getMyOfferings({ semesterId: 7 }),
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
        query: 'status=PENDING',
    },
    {
        name: 'offerings.requestEnrollment',
        route: 'POST /course-offerings/9/enrollments',
        call: (api) => api.offerings.requestEnrollment(9),
    },
    {
        name: 'enrollments.getMyEnrollments',
        route: 'GET /me/enrollments',
        call: (api) => api.enrollments.getMyEnrollments({ status: EnrollmentStatus.ENROLLED }),
        query: 'status=ENROLLED',
    },
    ...Object.values(EnrollmentAction).map(
        (action): Case => ({
            name: `enrollments.performAction (${action})`,
            route: `POST /enrollments/5/${action.toLowerCase()}`,
            call: (api) => api.enrollments.performAction({ enrollmentId: 5, action }),
        }),
    ),
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
