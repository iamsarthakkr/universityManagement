import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { enrollment, me } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { EnrollmentStatus } from '@/types/enrollment';
import { SemesterStatus, SemesterTerm } from '@/types/semester';

const WINTER = {
    id: 1,
    term: SemesterTerm.WINTER,
    year: 2026,
    status: SemesterStatus.ACTIVE,
    isRegistrationOpen: false,
};
const SUMMER = { ...WINTER, id: 2, term: SemesterTerm.SUMMER, status: SemesterStatus.COMPLETED };

function enrolled(id: number, code: string, credits: number, semester: typeof WINTER) {
    const base = enrollment();
    return enrollment({
        id,
        status: EnrollmentStatus.ENROLLED,
        courseOffering: { ...base.courseOffering, id, semester, course: { id, code, title: `${code} title`, credits } },
    });
}

function asStudent(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([]), 'GET /me': jsonOk(me('STUDENT')), ...routes });
}

describe('MyCoursesView', () => {
    it('groups enrolled courses by semester in server order, with credit totals', async () => {
        const { requests } = asStudent({
            'GET /me/enrollments': jsonOk([
                enrolled(1, 'CS101', 4, WINTER),
                enrolled(2, 'CS102', 3, WINTER),
                enrolled(3, 'PH101', 2, SUMMER),
            ]),
        });
        renderApp('/dashboard/student/courses');

        await screen.findByRole('article', { name: 'CS101' });
        const groups = screen.getAllByRole('region', { name: /\d{4}$/ });
        expect(groups.map((group) => group.getAttribute('aria-label'))).toEqual(['Winter 2026', 'Summer 2026']);
        expect(within(groups[0]).getByText('7 credits')).toBeInTheDocument();
        expect(within(groups[1]).getByText('2 credits')).toBeInTheDocument();
        expect(requests.find((request) => request.path === '/me/enrollments')?.query).toBe('status=ENROLLED');
    });

    it('points to browsing when the student has no courses', async () => {
        asStudent({ 'GET /me/enrollments': jsonOk([]) });
        renderApp('/dashboard/student/courses');

        expect(await screen.findByText('You are not enrolled in any course yet.')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Browse offerings' })).toHaveAttribute(
            'href',
            '/dashboard/student/offerings',
        );
    });

    it('shows the server error', async () => {
        asStudent({ 'GET /me/enrollments': jsonError(403, 'Access denied') });
        renderApp('/dashboard/student/courses');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
    });
});
