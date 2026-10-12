import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { formatDateTime } from '@/lib/date';
import { setToken } from '@/lib/session';
import { instructorRegistration, me, studentRegistration } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp, renderWithProviders } from '@/tests/render';
import { RegistrationAction, RegistrationStatus } from '@/types/registration';
import { RegistrationsView } from './RegistrationsView';

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([]), 'GET /me': jsonOk(me('ADMIN')), ...routes });
}

async function openActions(username: string) {
    const row = (await screen.findByText(username)).closest('tr') as HTMLElement;
    await userEvent.click(within(row).getByRole('button', { name: `Actions for ${username}` }));
}

describe('RegistrationsView', () => {
    it('lists student registrations filtered by status, with the date of birth column', async () => {
        const { requests } = mockFetch({ 'GET /student-registrations': jsonOk([studentRegistration(1)]) });
        renderWithProviders(<RegistrationsView kind="student" status={RegistrationStatus.PENDING} />);

        expect(await screen.findByText('student1')).toBeInTheDocument();
        expect(requests[0].query).toBe('status=PENDING');
        expect(screen.getByRole('heading', { name: 'Pending student registrations' })).toBeInTheDocument();
        expect(screen.getByRole('columnheader', { name: 'Date of Birth' })).toBeInTheDocument();
        expect(screen.getByText('6 May 2004')).toBeInTheDocument();
        expect(screen.getByText(formatDateTime('2026-10-01T08:00:00Z'))).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Reviewed' })).not.toBeInTheDocument();
    });

    it('shows when reviewed requests were reviewed and offers no actions', async () => {
        mockFetch({
            'GET /instructor-registrations': jsonOk([instructorRegistration(2, RegistrationStatus.APPROVED)]),
        });
        renderWithProviders(<RegistrationsView kind="instructor" status={RegistrationStatus.APPROVED} />);

        expect(await screen.findByText('instructor2')).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Date of Birth' })).not.toBeInTheDocument();
        expect(screen.getByText(formatDateTime('2026-10-03T08:00:00Z'))).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Actions' })).not.toBeInTheDocument();
    });

    it('offers only the actions the server allows', async () => {
        mockFetch({
            'GET /student-registrations': jsonOk([
                studentRegistration(4, RegistrationStatus.PENDING, { allowedActions: [RegistrationAction.REJECT] }),
            ]),
        });
        renderWithProviders(<RegistrationsView kind="student" status={RegistrationStatus.PENDING} />);

        await openActions('student4');

        const items = await screen.findAllByRole('menuitem');
        expect(items.map((item) => item.textContent)).toEqual(['Reject']);
    });

    it('approves a request and refreshes the list', async () => {
        let approved = false;
        const { requests } = asAdmin({
            'GET /student-registrations': () => jsonOk(approved ? [] : [studentRegistration(7)]),
            'POST /student-registrations/7/approve': () => {
                approved = true;
                return jsonOk(studentRegistration(7, RegistrationStatus.APPROVED));
            },
        });
        renderApp('/dashboard/admin/student-registrations/pending');

        await openActions('student7');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Approve' }));

        expect(await screen.findByText('No pending student registration requests.')).toBeInTheDocument();
        expect(screen.getByText('Request approved successfully!')).toBeInTheDocument();
        expect(requests.filter((request) => request.method === 'POST').map((request) => request.path)).toEqual([
            '/student-registrations/7/approve',
        ]);
    });

    it('shows the server message when an action is refused', async () => {
        asAdmin({
            'GET /instructor-registrations': jsonOk([instructorRegistration(8)]),
            'POST /instructor-registrations/8/reject': jsonError(409, 'Registration already reviewed'),
        });
        renderApp('/dashboard/admin/instructor-registrations/pending');

        await openActions('instructor8');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Reject' }));

        expect(await screen.findByText('Failed to reject request')).toBeInTheDocument();
        expect(screen.getByText('Registration already reviewed')).toBeInTheDocument();
    });

    it('shows the server error with a retry button', async () => {
        let attempts = 0;
        mockFetch({
            'GET /student-registrations': () => {
                attempts += 1;
                return attempts === 1 ? jsonError(403, 'Access denied') : jsonOk([studentRegistration(3)]);
            },
        });
        renderWithProviders(<RegistrationsView kind="student" status={RegistrationStatus.PENDING} />);

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', { name: 'Try again' }));
        await waitFor(() => expect(screen.getByText('student3')).toBeInTheDocument());
    });
});
