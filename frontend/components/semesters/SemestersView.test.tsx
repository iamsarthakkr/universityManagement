import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { me, semester } from '@/tests/fixtures';
import { SemesterAction, SemesterStatus, SemesterTerm } from '@/types/semester';

const SUMMER_2026 = semester({
    id: 2,
    term: SemesterTerm.SUMMER,
    status: SemesterStatus.COMPLETED,
    allowedActions: [],
    registrationStartDate: '2026-03-01',
    registrationEndDate: '2026-03-15',
    startDate: '2026-04-01',
    endDate: '2026-08-31',
});

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk([]),
        'GET /me': jsonOk(me('ADMIN')),
        ...routes,
    });
}

async function openActions(semesterName: string) {
    const row = (await screen.findByText(semesterName)).closest('tr') as HTMLElement;
    await userEvent.click(within(row).getByRole('button', { name: `Actions for ${semesterName}` }));
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

    it('links each semester to its detail page', async () => {
        asAdmin({ 'GET /semesters': jsonOk([semester()]) });
        renderApp('/dashboard/admin/semesters');

        expect(await screen.findByRole('link', { name: 'Winter 2026' })).toHaveAttribute(
            'href',
            '/dashboard/admin/semesters/1',
        );
    });

    it('offers only the transitions the server allows', async () => {
        asAdmin({ 'GET /semesters': jsonOk([semester(), SUMMER_2026]) });
        renderApp('/dashboard/admin/semesters');

        await openActions('Winter 2026');

        const items = await screen.findAllByRole('menuitem');
        expect(items.map((item) => item.textContent)).toEqual(['Activate', 'Cancel semester']);
        const summerRow = screen.getByText('Summer 2026').closest('tr') as HTMLElement;
        expect(within(summerRow).queryByRole('button', { name: 'Actions for Summer 2026' })).not.toBeInTheDocument();
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
            'POST /semesters/1/activate': () => {
                current = semester({
                    status: SemesterStatus.ACTIVE,
                    allowedActions: [SemesterAction.COMPLETE, SemesterAction.CANCEL],
                });
                return jsonOk(current);
            },
        });
        renderApp('/dashboard/admin/semesters');

        await openActions('Winter 2026');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Activate' }));

        expect(await screen.findByText('Semester activated.')).toBeInTheDocument();
        expect(await screen.findByText('ACTIVE')).toBeInTheDocument();
        expect(requests.filter((request) => request.method === 'POST').map((request) => request.path)).toEqual([
            '/semesters/1/activate',
        ]);
    });

    it('asks for confirmation before cancelling', async () => {
        const { requests } = asAdmin({
            'GET /semesters': jsonOk([semester()]),
            'POST /semesters/1/cancel': jsonOk(semester({ status: SemesterStatus.CANCELLED, allowedActions: [] })),
        });
        renderApp('/dashboard/admin/semesters');
        const posts = () => requests.filter((request) => request.method === 'POST');

        await openActions('Winter 2026');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Cancel semester' }));
        expect(await screen.findByRole('alertdialog', { name: 'Cancel Winter 2026?' })).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', { name: 'Keep semester' }));
        await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument());
        expect(posts()).toHaveLength(0);

        await openActions('Winter 2026');
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Cancel semester' }));
        await userEvent.click(await screen.findByRole('button', { name: 'Cancel semester' }));

        expect(await screen.findByText('Semester cancelled.')).toBeInTheDocument();
        expect(posts().map((request) => request.path)).toEqual(['/semesters/1/cancel']);
    });

    it('shows the server message when a transition is rejected', async () => {
        asAdmin({
            'GET /semesters': jsonOk([semester()]),
            'POST /semesters/1/activate': jsonError(400, 'Invalid semester transition: PLANNED -> ACTIVE'),
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
