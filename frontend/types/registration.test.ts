import { describe, expect, it } from 'vitest';

import { parseRegistrationStatus, RegistrationStatus } from './registration';

describe('parseRegistrationStatus', () => {
    it.each([
        ['pending', RegistrationStatus.PENDING],
        ['approved', RegistrationStatus.APPROVED],
        ['REJECTED', RegistrationStatus.REJECTED],
        ['Approved', RegistrationStatus.APPROVED],
    ])('parses %j', (value, expected) => {
        expect(parseRegistrationStatus(value)).toBe(expected);
    });

    it.each(['foo', '', undefined])('returns null for %j', (value) => {
        expect(parseRegistrationStatus(value)).toBeNull();
    });
});
