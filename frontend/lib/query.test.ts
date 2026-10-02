import { describe, expect, it } from 'vitest';

import { RemoteRes } from '@/types/common';
import { ApiError, queryClient, unwrap } from './query';

function result<T>(overrides: Partial<RemoteRes<T>>): Promise<RemoteRes<T>> {
    return Promise.resolve({ isSuccess: true, message: 'Success', status: 200, timestamp: new Date(), ...overrides });
}

describe('unwrap', () => {
    it('returns the body on success', async () => {
        await expect(unwrap(result({ body: [1, 2] }))).resolves.toEqual([1, 2]);
    });

    it('resolves to undefined for a success without a body', async () => {
        await expect(unwrap(result({ body: undefined, status: 204 }))).resolves.toBeUndefined();
    });

    it('throws an ApiError carrying the server message and status', async () => {
        const error = await unwrap(result({ isSuccess: false, status: 400, message: 'Email taken' })).catch(
            (caught) => caught,
        );

        expect(error).toBeInstanceOf(ApiError);
        expect(error).toMatchObject({ status: 400, message: 'Email taken' });
    });

    it('uses a fallback message when the server sends none', async () => {
        await expect(unwrap(result({ isSuccess: false, status: 403, message: '' }))).rejects.toThrow('Request failed.');
    });
});

describe('query retry policy', () => {
    const retry = queryClient.getDefaultOptions().queries?.retry as (count: number, error: Error) => boolean;

    it.each([400, 403, 404, 422])('never retries a %i', (status) => {
        expect(retry(0, new ApiError('x', status))).toBe(false);
    });

    it.each([0, 500, 503])('retries a %i once', (status) => {
        expect(retry(0, new ApiError('x', status))).toBe(true);
        expect(retry(1, new ApiError('x', status))).toBe(false);
    });
});
