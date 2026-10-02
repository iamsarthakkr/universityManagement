import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { RegistrationStatus, StudentRegistrationResponse } from '@/types/registration';
import { RegistrationsView } from './RegistrationsView';

const department = { id: 1, name: 'Computer Science', code: 'CS' };

function studentRegistration(id: number, status = RegistrationStatus.PENDING): StudentRegistrationResponse {
    return {
        id,
        username: `student${id}`,
        email: `student${id}@uni.edu`,
        firstName: 'Student',
        lastName: String(id),
        dateOfBirth: '2004-05-06',
        status,
        submittedAt: '2026-10-01',
        department,
    };
}

describe('RegistrationsView', () => {
    it('lists student registrations with the date of birth column', async () => {
        mockFetch({ 'GET /admin/student-registrations/PENDING': jsonOk([studentRegistration(1)]) });

        renderWithProviders(<RegistrationsView kind="student" status={RegistrationStatus.PENDING} />);

        expect(await screen.findByText('student1')).toBeInTheDocument();
        expect(screen.getByRole('heading', { name: 'Pending student registrations' })).toBeInTheDocument();
        expect(screen.getByRole('columnheader', { name: 'Date of Birth' })).toBeInTheDocument();
        expect(screen.getByText('2004-05-06')).toBeInTheDocument();
    });

    it('hides the date of birth column for instructors', async () => {
        mockFetch({
            'GET /admin/instructor-registrations/APPROVED': jsonOk([
                { ...studentRegistration(2, RegistrationStatus.APPROVED), dateOfBirth: undefined },
            ]),
        });

        renderWithProviders(<RegistrationsView kind="instructor" status={RegistrationStatus.APPROVED} />);

        expect(await screen.findByText('student2')).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Date of Birth' })).not.toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Actions' })).not.toBeInTheDocument();
    });

    it('approves a request and refreshes the list', async () => {
        let approved = false;
        const { requests } = mockFetch({
            'GET /admin/student-registrations/PENDING': () => jsonOk(approved ? [] : [studentRegistration(7)]),
            'POST /admin/student-registrations/7/approve': () => {
                approved = true;
                return jsonOk(studentRegistration(7, RegistrationStatus.APPROVED));
            },
        });

        renderWithProviders(<RegistrationsView kind="student" status={RegistrationStatus.PENDING} />);
        const row = (await screen.findByText('student7')).closest('tr') as HTMLElement;

        await userEvent.click(within(row).getByRole('button', { name: 'Open menu' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Approve' }));

        expect(await screen.findByText('No pending student registration requests.')).toBeInTheDocument();
        expect(requests.filter((request) => request.method === 'POST').map((request) => request.path)).toEqual([
            '/admin/student-registrations/7/approve',
        ]);
    });

    it('shows the server error with a retry button', async () => {
        let attempts = 0;
        mockFetch({
            'GET /admin/student-registrations/PENDING': () => {
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
