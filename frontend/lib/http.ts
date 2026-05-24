import { RemoteRes } from '@/types/common';

type RequestConfig = Omit<RequestInit, 'body'> & {
    body?: unknown;
};

const API_BASE_URL = process.env.API_BASE_URL ?? 'http://localhost:8080';

const DEFAULT_HEADERS: HeadersInit = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
};

function fallbackError<T>(message = 'Unknown error'): RemoteRes<T> {
    return {
        message,
        isSuccess: false,
        timestamp: new Date(),
    };
}

async function parseResponse<T>(response: Response): Promise<RemoteRes<T>> {
    const contentType = response.headers.get('content-type');

    if (contentType?.includes('application/json')) {
        const json = await response.json();

        return {
            message: json.message ?? response.statusText,
            body: json.body,
            errors: json.errors,
            isSuccess: json.isSuccess ?? response.ok,
            timestamp: json.timestamp ? new Date(json.timestamp) : new Date(),
        };
    }

    const text = await response.text();

    return {
        message: text || response.statusText,
        isSuccess: response.ok,
        timestamp: new Date(),
    };
}

async function request<T>(path: string, config: RequestConfig = {}): Promise<RemoteRes<T>> {
    try {
        const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;

        const headers = new Headers({
            ...DEFAULT_HEADERS,
            ...config.headers,
        });

        if (token) {
            headers.set('Authorization', `Bearer ${token}`);
        }

        const response = await fetch(`${API_BASE_URL}${path}`, {
            ...config,
            headers,
            body: config.body !== undefined ? JSON.stringify(config.body) : undefined,
        });

        return await parseResponse<T>(response);
    } catch {
        return fallbackError<T>();
    }
}

export const http = {
    get: <T>(path: string, config?: Omit<RequestConfig, 'method' | 'body'>): Promise<RemoteRes<T>> =>
        request<T>(path, {
            ...config,
            method: 'GET',
        }),

    post: <TResponse, TBody = unknown>(
        path: string,
        body?: TBody,
        config?: Omit<RequestConfig, 'method' | 'body'>,
    ): Promise<RemoteRes<TResponse>> =>
        request<TResponse>(path, {
            ...config,
            method: 'POST',
            body,
        }),

    put: <TResponse, TBody = unknown>(
        path: string,
        body?: TBody,
        config?: Omit<RequestConfig, 'method' | 'body'>,
    ): Promise<RemoteRes<TResponse>> =>
        request<TResponse>(path, {
            ...config,
            method: 'PUT',
            body,
        }),

    patch: <TResponse, TBody = unknown>(
        path: string,
        body?: TBody,
        config?: Omit<RequestConfig, 'method' | 'body'>,
    ): Promise<RemoteRes<TResponse>> =>
        request<TResponse>(path, {
            ...config,
            method: 'PATCH',
            body,
        }),

    delete: <T>(path: string, config?: Omit<RequestConfig, 'method' | 'body'>): Promise<RemoteRes<T>> =>
        request<T>(path, {
            ...config,
            method: 'DELETE',
        }),
};
