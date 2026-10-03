import { getToken } from '@/lib/session';
import { RemoteRes } from '@/types/common';

type RequestConfig = Omit<RequestInit, 'body'> & {
    body?: unknown;
    skipAuth?: boolean;
};

type ApiPayload = {
    message?: string;
    body?: unknown;
    errors?: Record<string, string>;
    isSuccess?: boolean;
    timestamp?: string;
};

export const API_BASE_URL = '/api';

let unauthorizedHandler: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null) {
    unauthorizedHandler = handler;
}

function errorResult<T>(message: string, status: number): RemoteRes<T> {
    return {
        message,
        isSuccess: false,
        timestamp: new Date(),
        status,
    };
}

function defaultMessage(response: Response) {
    if (response.statusText) {
        return response.statusText;
    }

    return response.ok ? 'OK' : `Request failed with status ${response.status}`;
}

async function parseResponse<T>(response: Response, requestLabel: string): Promise<RemoteRes<T>> {
    const text = await response.text();

    if (!text) {
        return {
            message: defaultMessage(response),
            isSuccess: response.ok,
            timestamp: new Date(),
            status: response.status,
        };
    }

    const contentType = response.headers.get('content-type');

    if (!contentType?.includes('application/json')) {
        return {
            message: response.ok ? text : defaultMessage(response),
            isSuccess: response.ok,
            timestamp: new Date(),
            status: response.status,
        };
    }

    let json: ApiPayload;
    try {
        json = JSON.parse(text);
    } catch (error) {
        console.error(`Invalid JSON response: ${requestLabel}`, error);
        return errorResult<T>('Received an invalid response from the server.', response.status);
    }

    return {
        message: json.message || defaultMessage(response),
        body: json.body as T | undefined,
        errors: json.errors,
        isSuccess: json.isSuccess ?? response.ok,
        timestamp: json.timestamp ? new Date(json.timestamp) : new Date(),
        status: response.status,
    };
}

async function request<T>(path: string, config: RequestConfig = {}): Promise<RemoteRes<T>> {
    const { skipAuth = false, body, ...init } = config;
    const requestLabel = `${init.method ?? 'GET'} ${path}`;
    const token = skipAuth ? null : getToken();

    const headers = new Headers(init.headers);
    if (!headers.has('Accept')) {
        headers.set('Accept', 'application/json');
    }
    if (body !== undefined && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
    }

    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }

    let response: Response;
    try {
        response = await fetch(`${API_BASE_URL}${path}`, {
            ...init,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch (error) {
        console.error(`Network error: ${requestLabel}`, error);
        return errorResult<T>('Unable to reach the server. Please check your connection and try again.', 0);
    }

    if (response.status === 401 && token) {
        unauthorizedHandler?.();
    }

    try {
        return await parseResponse<T>(response, requestLabel);
    } catch (error) {
        console.error(`Failed to read response: ${requestLabel}`, error);
        return errorResult<T>('Failed to read the server response.', response.status);
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
