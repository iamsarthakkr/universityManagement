import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { me, offering, semester } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { SemesterStatus, SemesterTerm } from '@/types/semester';

const SUMMER = {
    id: 2,
    term: SemesterTerm.SUMMER,
    year: 2026,
    status: SemesterStatus.COMPLETED,
    isRegistrationOpen: false,
};
const WINTER = {
    id: 1,
    term: SemesterTerm.WINTER,
    year: 2026,
    status: SemesterStatus.PLANNED,
    isRegistrationOpen: true,
};

const OFFERINGS = [
    offering({ id: 20, semester: SUMMER, course: { id: 11, code: 'CS102', title: 'Databases', credits: 3 } }),
    offering({ id: 9, semester: WINTER }),
];

function asInstructor(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk([]),
        'GET /me': jsonOk(me('INSTRUCTOR')),
        'GET /semesters': jsonOk([semester(), semester({ id: 2, term: SemesterTerm.SUMMER, startDate: '2026-04-01' })]),
        ...routes,
    });
}

describe('MyOfferingsView', () => {
    it('groups the offerings by semester, latest first, and links each to its students', async () => {
        asInstructor({ 'GET /me/course-offerings': jsonOk(OFFERINGS) });
        renderApp('/dashboard/instructor/courses');

        await screen.findByText('Databases');
        const groups = screen.getAllByRole('region', { name: /\d{4}$/ });
        expect(groups.map((group) => group.getAttribute('aria-label'))).toEqual(['Winter 2026', 'Summer 2026']);
        expect(within(groups[0]).getByText('Registration open')).toBeInTheDocument();
        expect(within(groups[1]).getByText('COMPLETED')).toBeInTheDocument();
        expect(within(groups[1]).getByRole('link', { name: 'CS102 section A' })).toHaveAttribute(
            'href',
            '/dashboard/offerings/20',
        );
    });

    it('filters by semester', async () => {
        const { requests } = asInstructor({ 'GET /me/course-offerings': jsonOk(OFFERINGS) });
        renderApp('/dashboard/instructor/courses');

        await screen.findByText('Databases');
        await waitFor(() => expect(screen.getByLabelText('Semester')).toBeEnabled());
        await userEvent.selectOptions(screen.getByLabelText('Semester'), 'Summer 2026');

        await waitFor(() =>
            expect(
                requests.filter((request) => request.path === '/me/course-offerings').map((request) => request.query),
            ).toEqual(['', 'semesterId=2']),
        );
    });

    it('shows the empty state for a filtered semester', async () => {
        asInstructor({ 'GET /me/course-offerings': jsonOk([]) });
        renderApp('/dashboard/instructor/courses?semesterId=1');

        expect(await screen.findByText('You teach no offerings in this semester.')).toBeInTheDocument();
    });

    it('shows the empty state without a filter', async () => {
        asInstructor({ 'GET /me/course-offerings': jsonOk([]) });
        renderApp('/dashboard/instructor/courses');

        expect(await screen.findByText('You have no offerings yet.')).toBeInTheDocument();
    });

    it('shows the server error', async () => {
        asInstructor({ 'GET /me/course-offerings': jsonError(403, 'Access denied') });
        renderApp('/dashboard/instructor/courses');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
    });
});
