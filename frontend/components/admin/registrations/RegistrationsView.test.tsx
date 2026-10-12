import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { formatDateTime } from '@/lib/date';
import { setToken } from '@/lib/session';
import { CS, instructorRegistration, me, PHYSICS, studentRegistration } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { RegistrationAction, RegistrationStatus } from '@/types/registration';

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([CS, PHYSICS]), 'GET /me': jsonOk(me('ADMIN')), ...routes });
}

async function chooseAction(username: string, action: string) {
    const row = (await screen.findByText(username)).closest('tr') as HTMLElement;
    await userEvent.click(within(row).getByRole('button', { name: `Actions for ${username}` }));
    await userEvent.click(await screen.findByRole('menuitem', { name: action }));
}

const listQueries = (requests: { path: string; query: string }[], path: string) =>
    requests.filter((request) => request.path === path).map((request) => request.query);

describe('RegistrationsView', () => {
    it('opens on pending requests, with the date of birth column for students', async () => {
        const { requests } = asAdmin({ 'GET /student-registrations': jsonOk([studentRegistration(1)]) });
        renderApp('/dashboard/admin/student-registrations');

        expect(await screen.findByText('student1')).toBeInTheDocument();
        expect(listQueries(requests, '/student-registrations')).toEqual(['status=PENDING']);
        expect(screen.getByRole('heading', { level: 1, name: 'Student registrations' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'Pending' })).toHaveAttribute('aria-selected', 'true');
        expect(screen.getByRole('columnheader', { name: 'Date of Birth' })).toBeInTheDocument();
        expect(screen.getByText('6 May 2004')).toBeInTheDocument();
        expect(screen.getByText(formatDateTime('2026-10-01T08:00:00Z'))).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Reviewed' })).not.toBeInTheDocument();
    });

    it('switches status with the tabs and filters by department', async () => {
        const { requests } = asAdmin({ 'GET /instructor-registrations': jsonOk([]) });
        const router = renderApp('/dashboard/admin/instructor-registrations');

        await screen.findByText('No pending instructor registration requests.');
        await userEvent.click(screen.getByRole('tab', { name: 'Approved' }));
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');

        expect(await screen.findByText('No approved instructor requests in this department.')).toBeInTheDocument();
        expect(router.state.location.search).toBe('?status=APPROVED&departmentId=2');
        expect(listQueries(requests, '/instructor-registrations')).toEqual([
            'status=PENDING',
            'status=APPROVED',
            'status=APPROVED&departmentId=2',
        ]);
    });

    it('shows when reviewed requests were reviewed and offers no actions', async () => {
        asAdmin({ 'GET /instructor-registrations': jsonOk([instructorRegistration(2, RegistrationStatus.APPROVED)]) });
        renderApp('/dashboard/admin/instructor-registrations?status=approved');

        expect(await screen.findByText('instructor2')).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Date of Birth' })).not.toBeInTheDocument();
        expect(screen.getByText(formatDateTime('2026-10-03T08:00:00Z'))).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Actions' })).not.toBeInTheDocument();
    });

    it('offers only the actions the server allows', async () => {
        asAdmin({
            'GET /student-registrations': jsonOk([
                studentRegistration(4, RegistrationStatus.PENDING, { allowedActions: [RegistrationAction.REJECT] }),
            ]),
        });
        renderApp('/dashboard/admin/student-registrations');

        const row = (await screen.findByText('student4')).closest('tr') as HTMLElement;
        await userEvent.click(within(row).getByRole('button', { name: 'Actions for student4' }));

        expect((await screen.findAllByRole('menuitem')).map((item) => item.textContent)).toEqual(['Reject']);
    });

    it('approves a request after confirmation and refreshes the list', async () => {
        let approved = false;
        const { requests } = asAdmin({
            'GET /student-registrations': () => jsonOk(approved ? [] : [studentRegistration(7)]),
            'POST /student-registrations/7/approve': () => {
                approved = true;
                return jsonOk(studentRegistration(7, RegistrationStatus.APPROVED));
            },
        });
        renderApp('/dashboard/admin/student-registrations');

        await chooseAction('student7', 'Approve');
        const dialog = await screen.findByRole('alertdialog', { name: 'Approve student7?' });
        await userEvent.click(within(dialog).getByRole('button', { name: 'Approve' }));

        expect(await screen.findByText('No pending student registration requests.')).toBeInTheDocument();
        expect(screen.getByText('Request approved successfully!')).toBeInTheDocument();
        expect(requests.filter((request) => request.method === 'POST').map((request) => request.path)).toEqual([
            '/student-registrations/7/approve',
        ]);
    });

    it('does nothing when the admin backs out of rejecting', async () => {
        const { requests } = asAdmin({ 'GET /student-registrations': jsonOk([studentRegistration(5)]) });
        renderApp('/dashboard/admin/student-registrations');

        await chooseAction('student5', 'Reject');
        await userEvent.click(await screen.findByRole('button', { name: 'Keep request' }));

        await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument());
        expect(requests.filter((request) => request.method === 'POST')).toHaveLength(0);
    });

    it('shows the server message when an action is refused', async () => {
        asAdmin({
            'GET /instructor-registrations': jsonOk([instructorRegistration(8)]),
            'POST /instructor-registrations/8/reject': jsonError(409, 'Registration already reviewed'),
        });
        renderApp('/dashboard/admin/instructor-registrations');

        await chooseAction('instructor8', 'Reject');
        const dialog = await screen.findByRole('alertdialog', { name: 'Reject instructor8?' });
        await userEvent.click(within(dialog).getByRole('button', { name: 'Reject' }));

        expect(await screen.findByText('Failed to reject request')).toBeInTheDocument();
        expect(screen.getByText('Registration already reviewed')).toBeInTheDocument();
    });

    it('shows the server error with a retry button', async () => {
        let attempts = 0;
        asAdmin({
            'GET /student-registrations': () => {
                attempts += 1;
                return attempts === 1 ? jsonError(403, 'Access denied') : jsonOk([studentRegistration(3)]);
            },
        });
        renderApp('/dashboard/admin/student-registrations');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', { name: 'Try again' }));
        await waitFor(() => expect(screen.getByText('student3')).toBeInTheDocument());
    });
});
