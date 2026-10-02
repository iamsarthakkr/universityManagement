import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { setToken } from '@/lib/session';
import { AuthUser, Role } from '@/types/auth';
import { jsonOk, mockFetch } from './mockFetch';
import { renderApp } from './render';

const DEPARTMENTS = [{ id: 1, name: 'Computer Science', code: 'CS' }];

function loggedInAs(role: Role) {
    const user: AuthUser = { id: '1', username: 'sam', email: 'sam@uni.edu', role };
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk(DEPARTMENTS),
        'GET /auth/me': jsonOk(user),
        'GET /admin/student-registrations/PENDING': jsonOk([]),
    });
}

describe('routing and guards', () => {
    it('sends logged-out users from the dashboard to the login page', async () => {
        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS) });
        const router = renderApp('/dashboard/admin');

        expect(await screen.findByText('Login with your credentials')).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/login');
    });

    it('sends logged-in users away from the login page to their home', async () => {
        loggedInAs('INSTRUCTOR');
        const router = renderApp('/login');

        expect(await screen.findByRole('heading', { name: 'Instructor dashboard' })).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/dashboard/instructor');
    });

    it.each<[Role, string]>([
        ['ADMIN', '/dashboard/admin'],
        ['INSTRUCTOR', '/dashboard/instructor'],
        ['STUDENT', '/dashboard/student'],
    ])('redirects %s from /dashboard to %s', async (role, home) => {
        loggedInAs(role);
        const router = renderApp('/dashboard');

        await screen.findByRole('heading', { name: /dashboard/i });
        expect(router.state.location.pathname).toBe(home);
    });

    it('keeps students out of admin pages', async () => {
        loggedInAs('STUDENT');
        const router = renderApp('/dashboard/admin/student-registrations/pending');

        expect(await screen.findByRole('heading', { name: 'Student dashboard' })).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/dashboard/student');
    });

    it('keeps instructors out of course creation', async () => {
        loggedInAs('INSTRUCTOR');
        const router = renderApp('/dashboard/courses/new');

        await screen.findByRole('heading', { name: 'Instructor dashboard' });
        expect(router.state.location.pathname).toBe('/dashboard/instructor');
    });

    it('redirects an unknown registration status to pending', async () => {
        loggedInAs('ADMIN');
        const router = renderApp('/dashboard/admin/student-registrations/foo');

        expect(await screen.findByRole('heading', { name: 'Pending student registrations' })).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/dashboard/admin/student-registrations/pending');
    });

    it('shows the error overlay when the backend is down and recovers on retry', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {});
        mockFetch({});
        renderApp('/login');

        expect(await screen.findByText('Something went wrong')).toBeInTheDocument();

        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS) });
        await userEvent.click(screen.getByRole('button', { name: 'Try again' }));

        expect(await screen.findByText('Login with your credentials')).toBeInTheDocument();
    });
});
