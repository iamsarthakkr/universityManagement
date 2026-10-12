import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { enrollment, me, offering } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { Role } from '@/types/auth';
import { EnrollmentAction, EnrollmentStatus } from '@/types/enrollment';

const PENDING = enrollment({ allowedActions: [EnrollmentAction.APPROVE, EnrollmentAction.REJECT] });
const ENROLLED = enrollment({
    id: 6,
    status: EnrollmentStatus.ENROLLED,
    student: { id: 8, firstName: 'Ben', lastName: null },
});

function as(role: Role, routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([]), 'GET /me': jsonOk(me(role)), ...routes });
}

const rosterQueries = (requests: { path: string; query: string }[]) =>
    requests.filter((request) => request.path === '/course-offerings/9/enrollments').map((request) => request.query);

describe('OfferingDetail', () => {
    it('shows the offering and every enrollment by default', async () => {
        as('ADMIN', {
            'GET /course-offerings/9': jsonOk(offering()),
            'GET /course-offerings/9/enrollments': jsonOk([PENDING, ENROLLED]),
        });
        renderApp('/dashboard/offerings/9');

        expect(await screen.findByRole('heading', { level: 1, name: 'CS101 · Algorithms' })).toBeInTheDocument();
        expect(screen.getByText('Section A · 4 credits')).toBeInTheDocument();
        expect(screen.getByText('Kavya Sen')).toBeInTheDocument();
        expect(screen.getByText('18 seats left')).toBeInTheDocument();
        expect(await screen.findByText('Ana Gomez')).toBeInTheDocument();
        expect(screen.getByText('Ben')).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'All' })).toHaveAttribute('aria-selected', 'true');
    });

    it('filters the roster by status tab', async () => {
        const { requests } = as('INSTRUCTOR', {
            'GET /course-offerings/9': jsonOk(offering()),
            'GET /course-offerings/9/enrollments': (request) =>
                jsonOk(request.query === 'status=PENDING' ? [PENDING] : [PENDING, ENROLLED]),
        });
        const router = renderApp('/dashboard/offerings/9');

        await screen.findByText('Ben');
        await userEvent.click(screen.getByRole('tab', { name: 'Pending' }));

        await waitFor(() => expect(screen.queryByText('Ben')).not.toBeInTheDocument());
        expect(router.state.location.search).toBe('?status=PENDING');
        expect(rosterQueries(requests)).toEqual(['', 'status=PENDING']);
    });

    it('says when a tab is empty', async () => {
        as('INSTRUCTOR', {
            'GET /course-offerings/9': jsonOk(offering()),
            'GET /course-offerings/9/enrollments': jsonOk([]),
        });
        renderApp('/dashboard/offerings/9?status=enrolled');

        expect(await screen.findByText('No students enrolled yet.')).toBeInTheDocument();
    });

    it('approves a request and refreshes the roster and seats', async () => {
        let approved = false;
        as('INSTRUCTOR', {
            'GET /course-offerings/9': () => jsonOk(offering(approved ? { enrolled: 13, seatsAvailable: 17 } : {})),
            'GET /course-offerings/9/enrollments': () =>
                jsonOk([approved ? { ...PENDING, status: EnrollmentStatus.ENROLLED, allowedActions: [] } : PENDING]),
            'POST /enrollments/5/approve': () => {
                approved = true;
                return jsonOk(PENDING);
            },
        });
        renderApp('/dashboard/offerings/9');

        await userEvent.click(await screen.findByRole('button', { name: 'Actions for Ana Gomez' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Approve' }));

        expect(await screen.findByText('Enrollment approved.')).toBeInTheDocument();
        expect(await screen.findByText('17 seats left')).toBeInTheDocument();
        expect(screen.getByText('ENROLLED')).toBeInTheDocument();
    });

    it('asks before rejecting', async () => {
        const { requests } = as('ADMIN', {
            'GET /course-offerings/9': jsonOk(offering()),
            'GET /course-offerings/9/enrollments': jsonOk([PENDING]),
            'POST /enrollments/5/reject': jsonOk(PENDING),
        });
        renderApp('/dashboard/offerings/9');

        await userEvent.click(await screen.findByRole('button', { name: 'Actions for Ana Gomez' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Reject' }));
        const dialog = await screen.findByRole('alertdialog', { name: "Reject Ana Gomez's request?" });
        await userEvent.click(within(dialog).getByRole('button', { name: 'Reject' }));

        expect(await screen.findByText('Request rejected.')).toBeInTheDocument();
        expect(requests.filter((request) => request.method === 'POST').map((request) => request.path)).toEqual([
            '/enrollments/5/reject',
        ]);
    });

    it('shows the server message when an approval is refused', async () => {
        as('ADMIN', {
            'GET /course-offerings/9': jsonOk(offering()),
            'GET /course-offerings/9/enrollments': jsonOk([PENDING]),
            'POST /enrollments/5/approve': jsonError(409, 'Course offering is full'),
        });
        renderApp('/dashboard/offerings/9');

        await userEvent.click(await screen.findByRole('button', { name: 'Actions for Ana Gomez' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Approve' }));

        expect(await screen.findByText('Failed to update enrollment')).toBeInTheDocument();
        expect(screen.getByText('Course offering is full')).toBeInTheDocument();
    });

    it('shows the server message when an instructor opens a roster they do not teach', async () => {
        as('INSTRUCTOR', {
            'GET /course-offerings/9': jsonOk(offering()),
            'GET /course-offerings/9/enrollments': jsonError(403, 'You do not have permission to perform this action'),
        });
        renderApp('/dashboard/offerings/9');

        expect(await screen.findByText('You do not have permission to perform this action')).toBeInTheDocument();
    });

    it('links admins back to the offering semester', async () => {
        as('ADMIN', {
            'GET /course-offerings/9': jsonOk(offering()),
            'GET /course-offerings/9/enrollments': jsonOk([]),
        });
        renderApp('/dashboard/offerings/9');

        expect(await screen.findByRole('link', { name: 'Winter 2026' })).toHaveAttribute(
            'href',
            '/dashboard/admin/semesters/1',
        );
    });

    it('links instructors back to their offerings', async () => {
        as('INSTRUCTOR', { 'GET /course-offerings/9': jsonError(404, 'Offering not found with id 9') });
        renderApp('/dashboard/offerings/9');

        expect(await screen.findByText('Offering not found with id 9')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'My offerings' })).toHaveAttribute(
            'href',
            '/dashboard/instructor/courses',
        );
    });

    it('keeps students out', async () => {
        as('STUDENT', {});
        const router = renderApp('/dashboard/offerings/9');

        await waitFor(() => expect(router.state.location.pathname).toBe('/dashboard/student'));
    });

    it('sends an invalid id to the dashboard', async () => {
        as('ADMIN', {});
        const router = renderApp('/dashboard/offerings/abc');

        await waitFor(() => expect(router.state.location.pathname).toBe('/dashboard/admin'));
    });
});
