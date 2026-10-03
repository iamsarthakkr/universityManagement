import { describe, expect, it } from 'vitest';

import { toLocalIsoDate } from './date';

describe('toLocalIsoDate', () => {
    it('formats using local time, zero-padded', () => {
        expect(toLocalIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
    });

    it('keeps the local calendar day just before and after midnight', () => {
        expect(toLocalIsoDate(new Date(2026, 9, 2, 23, 59))).toBe('2026-10-02');
        expect(toLocalIsoDate(new Date(2026, 9, 3, 0, 1))).toBe('2026-10-03');
    });
});
