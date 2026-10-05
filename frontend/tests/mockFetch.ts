import { vi } from 'vitest';

import { API_BASE_URL } from '@/lib/http';

type MockResponse = {
    status?: number;
    body?: unknown;
};

type RouteHandler = MockResponse | ((request: RecordedRequest) => MockResponse);

export type RecordedRequest = {
    method: string;
    path: string;
    // Query string without the leading `?`; routes match on `path` only.
    query: string;
    headers: Headers;
    body: unknown;
};

export function jsonOk(body: unknown, message = 'Success'): MockResponse {
    return { status: 200, body: { isSuccess: true, message, body } };
}

export function jsonError(status: number, message: string): MockResponse {
    return { status, body: { isSuccess: false, message } };
}

export function mockFetch(routes: Record<string, RouteHandler>) {
    const requests: RecordedRequest[] = [];

    const fetchMock = vi.fn(async (input: RequestInfo | URL, init: RequestInit = {}) => {
        const url = new URL(input.toString(), window.location.origin);
        if (!url.pathname.startsWith(`${API_BASE_URL}/`)) {
            throw new Error(`Request to ${url.pathname} does not go through ${API_BASE_URL}`);
        }

        const method = init.method ?? 'GET';
        const path = url.pathname.slice(API_BASE_URL.length);
        const request: RecordedRequest = {
            method,
            path,
            query: url.search.slice(1),
            headers: new Headers(init.headers),
            body: typeof init.body === 'string' ? JSON.parse(init.body) : undefined,
        };
        requests.push(request);

        const handler = routes[`${method} ${path}`];
        if (!handler) {
            throw new TypeError(`Failed to fetch: no mock for ${method} ${path}`);
        }

        const { status = 200, body } = typeof handler === 'function' ? handler(request) : handler;
        return new Response(body === undefined ? null : JSON.stringify(body), {
            status,
            headers: body === undefined ? undefined : { 'content-type': 'application/json' },
        });
    });

    vi.stubGlobal('fetch', fetchMock);
    return { requests };
}
