import { vi } from 'vitest';

type MockResponse = {
    status?: number;
    body?: unknown;
};

type RouteHandler = MockResponse | ((request: RecordedRequest) => MockResponse);

export type RecordedRequest = {
    method: string;
    path: string;
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
        const url = new URL(input.toString());
        const method = init.method ?? 'GET';
        const request: RecordedRequest = {
            method,
            path: url.pathname,
            headers: new Headers(init.headers),
            body: typeof init.body === 'string' ? JSON.parse(init.body) : undefined,
        };
        requests.push(request);

        const handler = routes[`${method} ${url.pathname}`];
        if (!handler) {
            throw new TypeError(`Failed to fetch: no mock for ${method} ${url.pathname}`);
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
