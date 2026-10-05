import { describe, expect, it } from 'vitest';

import { formatIsoDate, formatIsoDateRange, toLocalIsoDate } from './date';

describe('toLocalIsoDate', () => {
    it('formats using local time, zero-padded', () => {
        expect(toLocalIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
    });

    it('keeps the local calendar day just before and after midnight', () => {
        expect(toLocalIsoDate(new Date(2026, 9, 2, 23, 59))).toBe('2026-10-02');
        expect(toLocalIsoDate(new Date(2026, 9, 3, 0, 1))).toBe('2026-10-03');
    });
});

describe('formatIsoDate', () => {
    it('formats the calendar day without shifting it by timezone', () => {
        expect(formatIsoDate('2026-10-01')).toBe('1 Oct 2026');
        expect(formatIsoDate('2027-03-31')).toBe('31 Mar 2027');
    });

    it('formats a range', () => {
        expect(formatIsoDateRange('2026-10-01', '2026-10-15')).toBe('1 Oct 2026 – 15 Oct 2026');
    });
});
