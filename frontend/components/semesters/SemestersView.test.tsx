import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { Semester, SemesterStatus, SemesterTerm } from '@/types/semester';

function semester(overrides: Partial<Semester> = {}): Semester {
    return {
        id: 1,
        term: SemesterTerm.WINTER,
        year: 2026,
        status: SemesterStatus.PLANNED,
        allowedTransitions: [SemesterStatus.ACTIVE, SemesterStatus.CANCELLED],
        registrationStartDate: '2026-10-01',
        registrationEndDate: '2026-10-15',
        startDate: '2026-11-01',
        endDate: '2027-03-31',
        ...overrides,
    };
}

const SUMMER_2026 = semester({
    id: 2,
    term: SemesterTerm.SUMMER,
    status: SemesterStatus.COMPLETED,
    allowedTransitions: [],
    registrationStartDate: '2026-03-01',
    registrationEndDate: '2026-03-15',
    startDate: '2026-04-01',
    endDate: '2026-08-31',
});

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk([]),
        'GET /auth/me': jsonOk({ id: 1, username: 'admin', role: 'ADMIN' }),
        ...routes,
    });
}

async function openActions(semesterName: string) {
    const row = (await screen.findByText(semesterName)).closest('tr') as HTMLElement;
    await userEvent.click(within(row).getByRole('button', { name: 'Open menu' }));
}

describe('SemestersView', () => {
    it('lists semesters newest first with status and formatted dates', async () => {
        asAdmin({ 'GET /semesters': jsonOk([SUMMER_2026, semester()]) });
        renderApp('/dashboard/admin/semesters');

        await screen.findByText('Winter 2026');
        const rows = screen.getAllByRole('row').slice(1);

        expect(rows.map((row) => within(row).getAllByRole('cell')[0].textContent)).toEqual([
            'Winter 2026',
            'Summer 2026',
        ]);
        expect(within(rows[0]).getByText('PLANNED')).toBeInTheDocument();
        expect(within(rows[0]).getByText('1 Oct 2026 – 15 Oct 2026')).toBeInTheDocument();
        expect(within(rows[0]).getByText('1 Nov 2026 – 31 Mar 2027')).toBeInTheDocument();
    });

    it('offers only the transitions the server allows', async () => {
        asAdmin({ 'GET /semesters': jsonOk([semester(), SUMMER_2026]) });
        renderApp('/dashboard/admin/semesters');

        await openActions('Winter 2026');

        const items = await screen.findAllByRole('menuitem');
        expect(items.map((item) => item.textContent)).toEqual(['Activate', 'Cancel semester']);
        const summerRow = screen.getByText('Summer 2026').closest('tr') as HTMLElement;
        expect(within(summerRow).queryByRole('button', { name: 'Open menu' })).not.toBeInTheDocument();
    });

    it('hides the actions column when nothing can change', async () => {
        asAdmin({ 'GET /semesters': jsonOk([SUMMER_2026]) });
        renderApp('/dashboard/admin/semesters');

        await screen.findByText('Summer 2026');

        expect(screen.queryByRole('columnheader', { name: 'Actions' })).not.toBeInTheDocument();
    });

    it('activates a semester and refreshes the list', async () => {
        let current = semester();
        const { requests } = asAdmin({
            'GET /semesters': () => jsonOk([current]),
            'PATCH /semesters/1/status': () => {
                current = semester({
                    status: SemesterStatus.ACTIVE,
                    allowedTransitions: [SemesterStatus.COMPLETED, SemesterStatus.CANCELLED],
                });
                return jsonOk(undefined);
            },
        });
        renderApp('/dashboard/admin/semesters');

        await openActions('Winter 2026');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Activate' }));

        expect(await screen.findByText('Semester activated.')).toBeInTheDocument();
        expect(await screen.findByText('ACTIVE')).toBeInTheDocument();
        expect(requests.find((request) => request.method === 'PATCH')?.body).toEqual({ status: 'ACTIVE' });
    });

    it('asks for confirmation before cancelling', async () => {
        const { requests } = asAdmin({
            'GET /semesters': jsonOk([semester()]),
            'PATCH /semesters/1/status': jsonOk(undefined),
        });
        renderApp('/dashboard/admin/semesters');
        const patches = () => requests.filter((request) => request.method === 'PATCH');

        await openActions('Winter 2026');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Cancel semester' }));
        expect(await screen.findByRole('alertdialog', { name: 'Cancel Winter 2026?' })).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', { name: 'Keep semester' }));
        await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument());
        expect(patches()).toHaveLength(0);

        await openActions('Winter 2026');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Cancel semester' }));
        await userEvent.click(await screen.findByRole('button', { name: 'Cancel semester' }));

        expect(await screen.findByText('Semester cancelled.')).toBeInTheDocument();
        expect(patches().map((request) => request.body)).toEqual([{ status: 'CANCELLED' }]);
    });

    it('shows the server message when a transition is rejected', async () => {
        asAdmin({
            'GET /semesters': jsonOk([semester()]),
            'PATCH /semesters/1/status': jsonError(400, 'Invalid semester transition: PLANNED -> ACTIVE'),
        });
        renderApp('/dashboard/admin/semesters');

        await openActions('Winter 2026');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Activate' }));

        expect(await screen.findByText('Failed to update semester')).toBeInTheDocument();
        expect(screen.getByText('Invalid semester transition: PLANNED -> ACTIVE')).toBeInTheDocument();
    });

    it('shows an empty state', async () => {
        asAdmin({ 'GET /semesters': jsonOk([]) });
        renderApp('/dashboard/admin/semesters');

        expect(await screen.findByText('No semesters yet.')).toBeInTheDocument();
    });
});
