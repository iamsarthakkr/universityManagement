import { describe, expect, it, vi } from 'vitest';

import { setUnauthorizedHandler } from '@/lib/http';
import { queryClient } from '@/lib/query';
import { getToken, setToken } from '@/lib/session';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { AppState } from '@/types/app';
import { AuthUser } from '@/types/auth';
import { useAppStore } from './appStore';

const DEPARTMENTS = [{ id: 1, name: 'Computer Science', code: 'CS' }];
const USER: AuthUser = { id: '1', username: 'sam', email: 'sam@uni.edu', role: 'ADMIN' };

const actions = () => useAppStore.getState().actions;

function withUnauthorizedHandler() {
    setUnauthorizedHandler(actions().expireSession);
}

describe('appStore.init', () => {
    it('becomes READY with departments and no user when no token is stored', async () => {
        const { requests } = mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS) });

        await actions().init();

        const state = useAppStore.getState();
        expect(state.appState).toBe(AppState.READY);
        expect(state.user).toBeNull();
        expect(state.staticData.departments).toEqual(DEPARTMENTS);
        expect(requests.map((request) => request.path)).toEqual(['/departments']);
    });

    it('restores the session when a valid token is stored', async () => {
        setToken('valid');
        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS), 'GET /auth/me': jsonOk(USER) });

        await actions().init();

        expect(useAppStore.getState()).toMatchObject({ appState: AppState.READY, user: USER });
    });

    it('logs out and clears the token when the stored token is rejected', async () => {
        withUnauthorizedHandler();
        setToken('expired');
        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS), 'GET /auth/me': jsonError(401, 'Invalid token') });

        await actions().init();

        expect(useAppStore.getState()).toMatchObject({ appState: AppState.READY, user: null });
        expect(getToken()).toBeNull();
    });

    it('fails without logging out when the session check hits a server error', async () => {
        setToken('valid');
        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS), 'GET /auth/me': jsonError(500, 'Server error') });

        await actions().init();

        expect(useAppStore.getState()).toMatchObject({ appState: AppState.FAILED, error: 'Server error' });
        expect(getToken()).toBe('valid');
    });

    it('fails when the backend is unreachable, and restores the session on retry', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {});
        setToken('valid');
        mockFetch({});

        await actions().init();
        expect(useAppStore.getState().appState).toBe(AppState.FAILED);
        expect(getToken()).toBe('valid');

        mockFetch({ 'GET /departments': jsonOk(DEPARTMENTS), 'GET /auth/me': jsonOk(USER) });
        await actions().init();

        expect(useAppStore.getState()).toMatchObject({ appState: AppState.READY, user: USER, error: null });
    });
});

describe('appStore auth actions', () => {
    it('logs in, stores the token and sets the user', async () => {
        mockFetch({ 'POST /auth/login': jsonOk({ accessToken: 'new-token', user: USER }) });

        const result = await actions().login('sam', 'secret');

        expect(result.success).toBe(true);
        expect(getToken()).toBe('new-token');
        expect(useAppStore.getState().user).toEqual(USER);
    });

    it('returns the server message on a failed login and never sends a stale token', async () => {
        withUnauthorizedHandler();
        setToken('stale');
        const { requests } = mockFetch({ 'POST /auth/login': jsonError(401, 'Login failed') });

        const result = await actions().login('sam', 'wrong');

        expect(result).toEqual({ success: false, message: 'Login failed' });
        expect(requests[0].headers.has('Authorization')).toBe(false);
        expect(requests[0].body).toEqual({ username: 'sam', password: 'wrong' });
    });

    it('logout clears the token, the user and the query cache', () => {
        setToken('valid');
        useAppStore.setState({ user: USER });
        queryClient.setQueryData(['registrations', 'student', 'PENDING'], [{ id: 1 }]);

        actions().logout();

        expect(getToken()).toBeNull();
        expect(useAppStore.getState().user).toBeNull();
        expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    });

    it('expireSession clears the token, the user and the query cache', () => {
        setToken('valid');
        useAppStore.setState({ user: USER });
        queryClient.setQueryData(['courses', 'catalogue'], []);

        actions().expireSession();

        expect(getToken()).toBeNull();
        expect(useAppStore.getState().user).toBeNull();
        expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    });

    it('keeps the same actions object across state changes', () => {
        const before = actions();
        useAppStore.setState({ user: USER });

        expect(actions()).toBe(before);
    });
});
