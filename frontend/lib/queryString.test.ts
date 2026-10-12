import { describe, expect, it } from 'vitest';

import { withQuery } from './queryString';

describe('withQuery', () => {
    it('returns the path unchanged without params', () => {
        expect(withQuery('/courses')).toBe('/courses');
        expect(withQuery('/courses', {})).toBe('/courses');
    });

    it('skips unset values and keeps zero and false', () => {
        expect(withQuery('/x', { a: undefined, b: null, c: '', d: 0, e: false })).toBe('/x?d=0&e=false');
    });

    it('encodes values', () => {
        expect(withQuery('/x', { q: 'a b&c' })).toBe('/x?q=a+b%26c');
    });
});
