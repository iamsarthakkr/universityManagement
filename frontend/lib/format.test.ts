import { describe, expect, it } from 'vitest';

import { offering } from '@/tests/fixtures';
import { formatOffering, fullName } from './format';

describe('fullName', () => {
    it.each([
        [{ firstName: 'Ana', lastName: 'Gomez' }, 'Ana Gomez'],
        [{ firstName: 'Ana', lastName: null }, 'Ana'],
        [{ firstName: 'Ana', lastName: '' }, 'Ana'],
        [{ firstName: 'Ana' }, 'Ana'],
    ])('formats %j', (person, expected) => {
        expect(fullName(person)).toBe(expected);
    });
});

describe('formatOffering', () => {
    it('names the course, section and semester', () => {
        expect(formatOffering(offering())).toBe('CS101 · Section A · Winter 2026');
    });
});
