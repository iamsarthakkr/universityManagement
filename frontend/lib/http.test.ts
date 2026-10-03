import { describe, expect, it, vi } from 'vitest';

import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { http, setUnauthorizedHandler } from './http';
import { setToken } from './session';

function stubFetchResponse(response: Response) {
    vi.stubGlobal(
        'fetch',
        vi.fn(async () => response),
    );
}

describe('http response handling', () => {
    it('returns body and server message for a JSON success', async () => {
        mockFetch({ 'GET /items': jsonOk([1, 2], 'Fetched') });

        const res = await http.get<number[]>('/items');

        expect(res).toMatchObject({ isSuccess: true, status: 200, message: 'Fetched', body: [1, 2] });
    });

    it('treats an empty 204 response as success', async () => {
        stubFetchResponse(new Response(null, { status: 204 }));

        const res = await http.post('/items/1/approve');

        expect(res.isSuccess).toBe(true);
        expect(res.status).toBe(204);
    });

    it('treats an empty body with a JSON content type as success', async () => {
        stubFetchResponse(new Response('', { status: 200, headers: { 'content-type': 'application/json' } }));

        const res = await http.get('/items');

        expect(res.isSuccess).toBe(true);
    });

    it('surfaces the server message on a JSON error', async () => {
        mockFetch({ 'POST /registration/student': jsonError(400, 'Email taken') });

        const res = await http.post('/registration/student', {});

        expect(res).toMatchObject({ isSuccess: false, status: 400, message: 'Email taken' });
    });

    it('falls back to a status message when the error has no message', async () => {
        stubFetchResponse(
            new Response(JSON.stringify({ isSuccess: false }), {
                status: 500,
                headers: { 'content-type': 'application/json' },
            }),
        );

        const res = await http.get('/items');

        expect(res.message).toBe('Request failed with status 500');
    });

    it('never shows an HTML error page as the message', async () => {
        stubFetchResponse(
            new Response('<html>Bad Gateway</html>', { status: 502, headers: { 'content-type': 'text/html' } }),
        );

        const res = await http.get('/items');

        expect(res).toMatchObject({ isSuccess: false, status: 502, message: 'Request failed with status 502' });
    });

    it('reports invalid JSON without losing the status', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {});
        stubFetchResponse(new Response('{oops', { status: 200, headers: { 'content-type': 'application/json' } }));

        const res = await http.get('/items');

        expect(res).toMatchObject({
            isSuccess: false,
            status: 200,
            message: 'Received an invalid response from the server.',
        });
    });

    it('reports a network failure with status 0 instead of throwing', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {});
        mockFetch({});

        const res = await http.get('/items');

        expect(res).toMatchObject({ isSuccess: false, status: 0 });
        expect(res.message).toMatch(/unable to reach the server/i);
    });
});

describe('http request target', () => {
    it('sends requests to the same-origin /api prefix', async () => {
        const fetchMock = vi.fn(async () => new Response(null, { status: 204 }));
        vi.stubGlobal('fetch', fetchMock);

        await http.get('/departments?active=true');

        expect(fetchMock).toHaveBeenCalledWith('/api/departments?active=true', expect.anything());
    });
});

describe('http request headers', () => {
    it('sends Content-Type only when there is a body', async () => {
        const { requests } = mockFetch({ 'GET /items': jsonOk([]), 'POST /items': jsonOk({}) });

        await http.get('/items');
        await http.post('/items', { name: 'x' });

        expect(requests[0].headers.has('Content-Type')).toBe(false);
        expect(requests[0].headers.get('Accept')).toBe('application/json');
        expect(requests[1].headers.get('Content-Type')).toBe('application/json');
        expect(requests[1].body).toEqual({ name: 'x' });
    });

    it('keeps caller headers passed as a Headers instance', async () => {
        const { requests } = mockFetch({ 'GET /items': jsonOk([]) });

        await http.get('/items', { headers: new Headers({ 'X-Trace': 'abc' }) });

        expect(requests[0].headers.get('X-Trace')).toBe('abc');
    });

    it('attaches the stored token unless skipAuth is set', async () => {
        setToken('token-123');
        const { requests } = mockFetch({ 'GET /auth/me': jsonOk({}), 'POST /auth/login': jsonOk({}) });

        await http.get('/auth/me');
        await http.post('/auth/login', {}, { skipAuth: true });

        expect(requests[0].headers.get('Authorization')).toBe('Bearer token-123');
        expect(requests[1].headers.has('Authorization')).toBe(false);
    });
});

describe('http unauthorized handling', () => {
    it('calls the handler on a 401 for a request that carried a token', async () => {
        const handler = vi.fn();
        setUnauthorizedHandler(handler);
        setToken('expired');
        mockFetch({ 'GET /auth/me': jsonError(401, 'Invalid token') });

        await http.get('/auth/me');

        expect(handler).toHaveBeenCalledTimes(1);
    });

    it('does not call the handler on a 401 without a token', async () => {
        const handler = vi.fn();
        setUnauthorizedHandler(handler);
        mockFetch({ 'POST /auth/login': jsonError(401, 'Login failed') });

        await http.post('/auth/login', {}, { skipAuth: true });

        expect(handler).not.toHaveBeenCalled();
    });
});
