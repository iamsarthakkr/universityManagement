import { describe, expect, it, vi } from 'vitest';

import { clearToken, getToken, setToken } from './session';

describe('session token storage', () => {
    it('stores, reads and clears the token', () => {
        expect(getToken()).toBeNull();

        expect(setToken('abc')).toBe(true);
        expect(getToken()).toBe('abc');

        expect(clearToken()).toBe(true);
        expect(getToken()).toBeNull();
    });

    it('returns null instead of throwing when storage cannot be read', () => {
        vi.spyOn(console, 'error').mockImplementation(() => {});
        vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
            throw new Error('blocked');
        });

        expect(getToken()).toBeNull();
    });

    it('returns false when storage refuses to save', () => {
        vi.spyOn(console, 'error').mockImplementation(() => {});
        vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
            throw new Error('QuotaExceededError');
        });

        expect(setToken('abc')).toBe(false);
    });

    it('returns false when storage refuses to remove', () => {
        setToken('abc');
        vi.spyOn(console, 'error').mockImplementation(() => {});
        vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
            throw new Error('blocked');
        });

        expect(clearToken()).toBe(false);
    });
});
