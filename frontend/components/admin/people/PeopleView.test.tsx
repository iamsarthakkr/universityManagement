import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { CS, instructor, me, PHYSICS, student } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([CS, PHYSICS]), 'GET /me': jsonOk(me('ADMIN')), ...routes });
}

describe('PeopleView', () => {
    it('lists students with email and department', async () => {
        asAdmin({
            'GET /students': jsonOk([
                student(),
                student({ id: 8, firstName: 'Ben', lastName: null, department: PHYSICS }),
            ]),
        });
        renderApp('/dashboard/admin/students');

        const row = (await screen.findByText('Ana Gomez')).closest('tr') as HTMLElement;
        expect(within(row).getByText('ana@uni.edu')).toBeInTheDocument();
        expect(within(row).getByText('Computer Science')).toBeInTheDocument();
        expect(screen.getByText('Ben')).toBeInTheDocument();
    });

    it('lists instructors and filters them by department', async () => {
        const { requests } = asAdmin({ 'GET /instructors': jsonOk([instructor()]) });
        const router = renderApp('/dashboard/admin/instructors');

        await screen.findByText('Kavya Sen');
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');

        await waitFor(() => expect(router.state.location.search).toBe('?departmentId=2'));
        expect(requests.filter((request) => request.path === '/instructors').map((request) => request.query)).toEqual([
            '',
            'departmentId=2',
        ]);
    });

    it('shows the empty state for a department', async () => {
        asAdmin({ 'GET /students': jsonOk([]) });
        renderApp('/dashboard/admin/students?departmentId=1');

        expect(await screen.findByText('No students in this department.')).toBeInTheDocument();
    });

    it('shows the empty state without a department', async () => {
        asAdmin({ 'GET /instructors': jsonOk([]) });
        renderApp('/dashboard/admin/instructors');

        expect(await screen.findByText('No instructors yet.')).toBeInTheDocument();
    });

    it('shows the server error', async () => {
        asAdmin({ 'GET /students': jsonError(403, 'Access denied') });
        renderApp('/dashboard/admin/students');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
    });
});
