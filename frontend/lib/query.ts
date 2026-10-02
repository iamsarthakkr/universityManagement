import { QueryClient } from '@tanstack/react-query';

import { RemoteRes } from '@/types/common';

export class ApiError extends Error {
    readonly status: number;

    constructor(message: string, status: number) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

export async function unwrap<T>(request: Promise<RemoteRes<T>>): Promise<T> {
    const res = await request;

    if (!res.isSuccess) {
        throw new ApiError(res.message || 'Request failed.', res.status);
    }

    return res.body as T;
}

function isClientError(error: Error) {
    return error instanceof ApiError && error.status >= 400 && error.status < 500;
}

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: (failureCount, error) => !isClientError(error) && failureCount < 1,
        },
    },
});
