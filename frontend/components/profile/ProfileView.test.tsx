import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { me } from '@/tests/fixtures';
import { jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { Me } from '@/types/auth';

function loggedIn(user: Me) {
    setToken('valid');
    mockFetch({
        'GET /departments': jsonOk([]),
        'GET /me': jsonOk(user),
        'GET /me/enrollments': jsonOk([]),
        'GET /semesters': jsonOk([]),
    });
}

const detail = (label: string) => screen.getByText(label).nextElementSibling?.textContent;

describe('ProfileView', () => {
    it('shows a student’s account and profile', async () => {
        loggedIn(me('STUDENT'));
        renderApp('/dashboard/profile');

        await screen.findByRole('heading', { level: 1, name: 'Profile' });
        expect(detail('Name')).toBe('Sam Rao');
        expect(detail('Role')).toBe('Student');
        expect(detail('Username')).toBe('sam');
        expect(detail('Email')).toBe('sam@uni.edu');
        expect(detail('Department')).toBe('Computer Science');
    });

    it('shows only account details for admins', async () => {
        loggedIn(me('ADMIN'));
        renderApp('/dashboard/profile');

        await screen.findByRole('heading', { level: 1, name: 'Profile' });
        expect(detail('Role')).toBe('Administrator');
        expect(screen.queryByText('Department')).not.toBeInTheDocument();
        expect(screen.queryByText('Name')).not.toBeInTheDocument();
    });

    it('opens from the user menu', async () => {
        loggedIn(me('STUDENT'));
        const router = renderApp('/dashboard/student');

        await userEvent.click(await screen.findByRole('button', { name: /Sam Rao/ }));
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Profile' }));

        expect(await screen.findByRole('heading', { level: 1, name: 'Profile' })).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/dashboard/profile');
    });
});
