import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { me, offering, semester } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { SemesterAction, SemesterStatus } from '@/types/semester';

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([]), 'GET /me': jsonOk(me('ADMIN')), ...routes });
}

describe('SemesterDetail', () => {
    it('shows the semester, its windows and its offerings', async () => {
        const { requests } = asAdmin({
            'GET /semesters/1': jsonOk(semester({ isRegistrationOpen: true })),
            'GET /course-offerings': jsonOk([offering()]),
        });
        renderApp('/dashboard/admin/semesters/1');

        expect(await screen.findByRole('heading', { level: 1, name: 'Winter 2026' })).toBeInTheDocument();
        expect(screen.getByText('PLANNED')).toBeInTheDocument();
        expect(screen.getByText('1 Oct 2026 – 15 Oct 2026')).toBeInTheDocument();
        expect(screen.getByText('Open now')).toBeInTheDocument();
        expect(screen.getByText('Accepted')).toBeInTheDocument();
        expect(await screen.findByText('CS101')).toBeInTheDocument();
        expect(screen.queryByRole('columnheader', { name: 'Semester' })).not.toBeInTheDocument();
        expect(requests.find((request) => request.path === '/course-offerings')?.query).toBe('semesterId=1');
        expect(screen.getByRole('link', { name: 'Add offering' })).toHaveAttribute(
            'href',
            '/dashboard/admin/offerings/new?semesterId=1',
        );
    });

    it('hides "Add offering" when the semester does not accept offerings', async () => {
        asAdmin({
            'GET /semesters/1': jsonOk(
                semester({ status: SemesterStatus.COMPLETED, acceptsOfferings: false, allowedActions: [] }),
            ),
            'GET /course-offerings': jsonOk([]),
        });
        renderApp('/dashboard/admin/semesters/1');

        expect(await screen.findByText('No offerings in this semester yet.')).toBeInTheDocument();
        expect(screen.getByText('Not accepted')).toBeInTheDocument();
        expect(screen.getByText('Closed')).toBeInTheDocument();
        expect(screen.queryByRole('link', { name: 'Add offering' })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Actions for Winter 2026' })).not.toBeInTheDocument();
    });

    it('runs a semester action from the header', async () => {
        let current = semester();
        asAdmin({
            'GET /semesters/1': () => jsonOk(current),
            'GET /course-offerings': jsonOk([]),
            'POST /semesters/1/activate': () => {
                current = semester({ status: SemesterStatus.ACTIVE, allowedActions: [SemesterAction.COMPLETE] });
                return jsonOk(current);
            },
        });
        renderApp('/dashboard/admin/semesters/1');

        await userEvent.click(await screen.findByRole('button', { name: 'Actions for Winter 2026' }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Activate' }));

        expect(await screen.findByText('Semester activated.')).toBeInTheDocument();
        expect(await screen.findByText('ACTIVE')).toBeInTheDocument();
    });

    it('shows the server error for an unknown semester', async () => {
        asAdmin({ 'GET /semesters/99': jsonError(404, 'Semester not found with id 99') });
        renderApp('/dashboard/admin/semesters/99');

        expect(await screen.findByText('Semester not found with id 99')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'All semesters' })).toBeInTheDocument();
    });

    it('sends an invalid id back to the list', async () => {
        asAdmin({ 'GET /semesters': jsonOk([]) });
        const router = renderApp('/dashboard/admin/semesters/abc');

        await waitFor(() => expect(router.state.location.pathname).toBe('/dashboard/admin/semesters'));
    });
});
