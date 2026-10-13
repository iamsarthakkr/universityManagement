import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { enrollment, me } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { EnrollmentAction, EnrollmentStatus } from '@/types/enrollment';

const PENDING = enrollment({ allowedActions: [EnrollmentAction.CANCEL] });
const ENROLLED = enrollment({
    id: 6,
    status: EnrollmentStatus.ENROLLED,
    allowedActions: [EnrollmentAction.DROP],
    courseOffering: {
        ...enrollment().courseOffering,
        id: 10,
        course: { id: 11, code: 'CS102', title: 'Databases', credits: 3 },
    },
});

function asStudent(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk([]),
        'GET /me': jsonOk(me('STUDENT')),
        'GET /semesters': jsonOk([]),
        ...routes,
    });
}

const row = (code: string) => screen.getByText(code).closest('tr') as HTMLElement;

describe('MyEnrollmentsView', () => {
    it('lists every request with its course, semester and status', async () => {
        asStudent({ 'GET /me/enrollments': jsonOk([PENDING, ENROLLED]) });
        renderApp('/dashboard/student/enrollments');

        await screen.findByText('CS101');
        expect(within(row('CS101')).getByText('PENDING')).toBeInTheDocument();
        expect(within(row('CS101')).getByText('Winter 2026')).toBeInTheDocument();
        expect(within(row('CS102')).getByText('ENROLLED')).toBeInTheDocument();
    });

    it('filters by status through the URL', async () => {
        const { requests } = asStudent({ 'GET /me/enrollments': jsonOk([PENDING]) });
        const router = renderApp('/dashboard/student/enrollments');

        await screen.findByText('CS101');
        await userEvent.selectOptions(screen.getByLabelText('Status'), 'Pending');

        await waitFor(() => expect(router.state.location.search).toBe('?status=PENDING'));
        expect(
            requests.filter((request) => request.path === '/me/enrollments').map((request) => request.query),
        ).toEqual(['', 'status=PENDING']);
    });

    it('drops a course after confirmation', async () => {
        let dropped = false;
        asStudent({
            'GET /me/enrollments': () =>
                jsonOk([dropped ? { ...ENROLLED, status: EnrollmentStatus.DROPPED, allowedActions: [] } : ENROLLED]),
            'POST /enrollments/6/drop': () => {
                dropped = true;
                return jsonOk(ENROLLED);
            },
        });
        renderApp('/dashboard/student/enrollments');

        await userEvent.click(await screen.findByRole('button', { name: 'Actions for CS102' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Drop course' }));
        const dialog = await screen.findByRole('alertdialog', { name: 'Drop CS102?' });
        await userEvent.click(within(dialog).getByRole('button', { name: 'Drop course' }));

        expect(await screen.findByText('Course dropped.')).toBeInTheDocument();
        expect(await screen.findByText('DROPPED')).toBeInTheDocument();
    });

    it('keeps the request when the student backs out of cancelling', async () => {
        const { requests } = asStudent({ 'GET /me/enrollments': jsonOk([PENDING]) });
        renderApp('/dashboard/student/enrollments');

        await userEvent.click(await screen.findByRole('button', { name: 'Actions for CS101' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Cancel request' }));
        await userEvent.click(await screen.findByRole('button', { name: 'Keep request' }));

        await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument());
        expect(requests.filter((request) => request.method === 'POST')).toHaveLength(0);
    });

    it('shows the server message when an action is refused', async () => {
        asStudent({
            'GET /me/enrollments': jsonOk([PENDING]),
            'POST /enrollments/5/cancel': jsonError(400, 'Semester no longer allows enrollment changes'),
        });
        renderApp('/dashboard/student/enrollments');

        await userEvent.click(await screen.findByRole('button', { name: 'Actions for CS101' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Cancel request' }));
        await userEvent.click(await screen.findByRole('button', { name: 'Cancel request' }));

        expect(await screen.findByText('Semester no longer allows enrollment changes')).toBeInTheDocument();
    });

    it('shows the empty state for filters that match nothing', async () => {
        asStudent({ 'GET /me/enrollments': jsonOk([]) });
        renderApp('/dashboard/student/enrollments?status=dropped');

        expect(await screen.findByText('No enrollments match these filters.')).toBeInTheDocument();
    });

    it('shows the empty state without filters', async () => {
        asStudent({ 'GET /me/enrollments': jsonOk([]) });
        renderApp('/dashboard/student/enrollments');

        expect(await screen.findByText('You have not requested any courses yet.')).toBeInTheDocument();
    });
});
