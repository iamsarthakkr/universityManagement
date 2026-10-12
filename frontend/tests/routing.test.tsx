import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { getSidebarNav } from '@/config/navigation/sidebar';
import { setToken } from '@/lib/session';
import { Role } from '@/types/auth';
import { me } from './fixtures';
import { jsonOk, mockFetch } from './mockFetch';
import { renderApp } from './render';

const DEPARTMENTS = [{ id: 1, name: 'Computer Science', code: 'CS' }];

function loggedInAs(role: Role) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk(DEPARTMENTS),
        'GET /me': jsonOk(me(role)),
        'GET /student-registrations': jsonOk([]),
        'GET /instructor-registrations': jsonOk([]),
        'GET /courses': jsonOk([]),
        'GET /semesters': jsonOk([]),
    });
}

function sidebarUrls(role: Role) {
    return getSidebarNav(role).flatMap((entry) =>
        'items' in entry ? entry.items.map((item) => item.url) : [entry.url],
    );
}

describe('routing and guards', () => {
    it('sends logged-out users from the dashboard to the login page', async () => {
        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS) });
        const router = renderApp('/dashboard/admin');

        expect(await screen.findByText('Login with your credentials')).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/login');
    });

    it('lets logged-out users reach both registration pages from the login page', async () => {
        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS) });
        const router = renderApp('/login');

        await userEvent.click(await screen.findByRole('link', { name: 'Student registration' }));
        expect(await screen.findByText('Submit student registration request for approval.')).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/registration/student');

        await userEvent.click(screen.getByRole('link', { name: 'Login' }));
        await userEvent.click(await screen.findByRole('link', { name: 'Instructor registration' }));
        expect(await screen.findByText('Submit instructor registration request for approval.')).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/registration/instructor');
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

describe('sidebar links', () => {
    const cases = (['ADMIN', 'INSTRUCTOR', 'STUDENT'] as Role[]).flatMap((role) =>
        sidebarUrls(role).map((url) => [role, url] as const),
    );

    it.each(cases)('%s can open %s', async (role, url) => {
        loggedInAs(role);
        const router = renderApp(url);

        expect(await screen.findByRole('heading', { level: 1 })).toBeInTheDocument();
        expect(router.state.location.pathname).toBe(url);
    });
});
