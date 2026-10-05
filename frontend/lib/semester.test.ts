import { describe, expect, it } from 'vitest';

import { SemesterTerm } from '@/types/semester';
import { formatSemesterName } from './semester';

describe('formatSemesterName', () => {
    it.each([
        [SemesterTerm.WINTER, 2026, 'Winter 2026'],
        [SemesterTerm.SUMMER, 2027, 'Summer 2027'],
    ])('%s %d -> %s', (term, year, expected) => {
        expect(formatSemesterName({ term, year })).toBe(expected);
    });
});
