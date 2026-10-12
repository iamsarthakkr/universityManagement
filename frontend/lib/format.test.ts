import { describe, expect, it } from 'vitest';

import { fullName } from './format';

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
