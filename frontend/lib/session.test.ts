import { describe, expect, it, vi } from 'vitest';

import { clearToken, getToken, setToken } from './session';

describe('session token storage', () => {
    it('stores, reads and clears the token', () => {
        expect(getToken()).toBeNull();

        setToken('abc');
        expect(getToken()).toBe('abc');

        clearToken();
        expect(getToken()).toBeNull();
    });

    it('returns null instead of throwing when storage is blocked', () => {
        vi.spyOn(console, 'error').mockImplementation(() => {});
        vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
            throw new Error('blocked');
        });

        expect(getToken()).toBeNull();
    });
});
