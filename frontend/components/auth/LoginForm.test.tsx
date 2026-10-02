import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { getToken } from '@/lib/session';
import { useAppStore } from '@/stores/appStore';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { LoginForm } from './LoginForm';

async function submitLogin(username: string, password: string) {
    await userEvent.type(screen.getByLabelText('Username'), username);
    await userEvent.type(screen.getByLabelText('Password'), password);
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }));
}

describe('LoginForm', () => {
    it('shows the server message when login fails', async () => {
        mockFetch({ 'POST /auth/login': jsonError(401, 'Invalid username or password') });
        renderWithProviders(<LoginForm />);

        await submitLogin('sam', 'wrong');

        expect(await screen.findByRole('alert')).toHaveTextContent('Invalid username or password');
        expect(useAppStore.getState().user).toBeNull();
    });

    it('logs the user in on success', async () => {
        const user = { id: '1', username: 'sam', email: 'sam@uni.edu', role: 'STUDENT' as const };
        mockFetch({ 'POST /auth/login': jsonOk({ accessToken: 'token', user }) });
        renderWithProviders(<LoginForm />);

        await submitLogin('sam', 'secret');

        expect(useAppStore.getState().user).toEqual(user);
        expect(getToken()).toBe('token');
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });
});
