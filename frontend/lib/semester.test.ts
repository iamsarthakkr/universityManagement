import { describe, expect, it } from 'vitest';

import { SemesterStatus, SemesterTerm } from '@/types/semester';
import { formatSemesterName, groupBySemester } from './semester';

describe('formatSemesterName', () => {
    it.each([
        [SemesterTerm.WINTER, 2026, 'Winter 2026'],
        [SemesterTerm.SUMMER, 2027, 'Summer 2027'],
    ])('%s %d -> %s', (term, year, expected) => {
        expect(formatSemesterName({ term, year })).toBe(expected);
    });
});

describe('groupBySemester', () => {
    const winter = {
        id: 1,
        term: SemesterTerm.WINTER,
        year: 2026,
        status: SemesterStatus.PLANNED,
        isRegistrationOpen: false,
    };
    const summer = { ...winter, id: 2, term: SemesterTerm.SUMMER };

    it('groups items by semester in arrival order', () => {
        const items = [
            { name: 'a', semester: summer },
            { name: 'b', semester: winter },
            { name: 'c', semester: summer },
        ];

        expect(groupBySemester(items, (item) => item.semester)).toEqual([
            { semester: summer, items: [items[0], items[2]] },
            { semester: winter, items: [items[1]] },
        ]);
    });

    it('returns no groups for no items', () => {
        expect(groupBySemester([], () => winter)).toEqual([]);
    });
});
