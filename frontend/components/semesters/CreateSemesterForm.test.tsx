import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk([]),
        'GET /auth/me': jsonOk({ id: 1, username: 'admin', role: 'ADMIN' }),
        ...routes,
    });
}

async function fillForm() {
    await userEvent.selectOptions(await screen.findByLabelText('Term'), 'Summer');
    await userEvent.clear(screen.getByLabelText('Year'));
    await userEvent.type(screen.getByLabelText('Year'), '2027');
    await userEvent.type(screen.getByLabelText('Opens'), '2027-03-01');
    await userEvent.type(screen.getByLabelText('Closes'), '2027-03-15');
    await userEvent.type(screen.getByLabelText('Starts'), '2027-04-01');
    await userEvent.type(screen.getByLabelText('Ends'), '2027-08-31');
}

const REQUEST = {
    term: 'SUMMER',
    year: 2027,
    registrationStartDate: '2027-03-01',
    registrationEndDate: '2027-03-15',
    startDate: '2027-04-01',
    endDate: '2027-08-31',
};

describe('CreateSemesterForm', () => {
    it('creates the semester and returns to the list', async () => {
        const created = { id: 5, ...REQUEST, status: 'PLANNED', allowedTransitions: ['ACTIVE', 'CANCELLED'] };
        const { requests } = asAdmin({
            'POST /semesters': jsonOk(created),
            'GET /semesters': jsonOk([created]),
        });
        const router = renderApp('/dashboard/admin/semesters/new');

        await fillForm();
        await userEvent.click(screen.getByRole('button', { name: 'Create semester' }));

        expect(await screen.findByText('Summer 2027 created.')).toBeInTheDocument();
        await waitFor(() => expect(router.state.location.pathname).toBe('/dashboard/admin/semesters'));
        expect(requests.find((request) => request.method === 'POST')?.body).toEqual(REQUEST);
    });

    it('keeps the form and shows the server message when creation fails', async () => {
        asAdmin({ 'POST /semesters': jsonError(409, 'Request violates a database constraint') });
        const router = renderApp('/dashboard/admin/semesters/new');

        await fillForm();
        await userEvent.click(screen.getByRole('button', { name: 'Create semester' }));

        expect(await screen.findByText('Failed to create semester')).toBeInTheDocument();
        expect(screen.getByText('Request violates a database constraint')).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/dashboard/admin/semesters/new');
        expect(screen.getByLabelText('Year')).toHaveValue(2027);
        expect(screen.getByRole('button', { name: 'Create semester' })).toBeEnabled();
    });
});
