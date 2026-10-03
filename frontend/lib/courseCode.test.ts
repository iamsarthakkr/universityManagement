import { describe, expect, it } from 'vitest';

import { normalizeCodeSuffix } from './courseCode';

describe('normalizeCodeSuffix', () => {
    it.each([
        ['101', 'CS', '101'],
        ['cs101', 'CS', '101'],
        [' CS 101 ', 'CS', '101'],
        ['MBA210', 'MBA', '210'],
        ['CS', 'CS', ''],
        ['', 'CS', ''],
    ])('normalizes %j for department %s to %j', (input, departmentCode, expected) => {
        expect(normalizeCodeSuffix(input, departmentCode)).toBe(expected);
    });
});
