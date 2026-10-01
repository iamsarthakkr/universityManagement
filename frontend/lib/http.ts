import { RemoteRes } from '@/types/common';

type RequestConfig = Omit<RequestInit, 'body'> & {
    body?: unknown;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

let unauthorizedHandler: (() => void) | null = null;

/**
 * Registers the callback fired when an authenticated request gets a 401
 * (expired/invalid token). Kept as a plain callback so this module never
 * depends on React or auth state; AuthProvider owns what "logout" means.
 */
export function setUnauthorizedHandler(handler: (() => void) | null) {
    unauthorizedHandler = handler;
}

const DEFAULT_HEADERS: HeadersInit = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
};

function fallbackError<T>(message = 'Unknown error'): RemoteRes<T> {
    return {
        message,
        isSuccess: false,
        timestamp: new Date(),
        status: 0,
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
            status: response.status,
        };
    }

    const text = await response.text();

    return {
        message: text || response.statusText,
        isSuccess: response.ok,
        timestamp: new Date(),
        status: response.status,
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

        // Only a 401 on a request that carried a token means the session is gone.
        // A 401 without a token (e.g. wrong password on /auth/login) is a normal failure.
        if (response.status === 401 && token) {
            unauthorizedHandler?.();
        }

        return await parseResponse<T>(response);
    } catch (error) {
        console.error(`Request failed: ${config.method ?? 'GET'} ${path}`, error);
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
