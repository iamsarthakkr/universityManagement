import { screen, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { instructorRegistration, me, offering, semester, studentRegistration } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';

function asAdmin(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([]), 'GET /me': jsonOk(me('ADMIN')), ...routes });
}

const stat = (label: string) => screen.getByText(label).closest('a') as HTMLAnchorElement;
const statValue = (label: string) => screen.getByText(label).nextElementSibling?.textContent;

describe('AdminHome', () => {
    it('counts pending requests, open semesters and offerings, each linking to its page', async () => {
        const { requests } = asAdmin({
            'GET /student-registrations': jsonOk([studentRegistration(1), studentRegistration(2)]),
            'GET /instructor-registrations': jsonOk([instructorRegistration(3)]),
            'GET /semesters': jsonOk([semester({ isRegistrationOpen: true }), semester({ id: 2 })]),
            'GET /course-offerings': jsonOk([offering(), offering({ id: 10 }), offering({ id: 11 })]),
        });
        renderApp('/dashboard/admin');

        await waitFor(() => expect(statValue('Offerings scheduled')).toBe('3'));
        expect(statValue('Student requests pending')).toBe('2');
        expect(statValue('Instructor requests pending')).toBe('1');
        expect(statValue('Semesters open for registration')).toBe('1');
        expect(stat('Student requests pending')).toHaveAttribute('href', '/dashboard/admin/student-registrations');
        expect(
            requests.filter((request) => request.path.endsWith('-registrations')).map((request) => request.query),
        ).toEqual(['status=PENDING', 'status=PENDING']);
    });

    it('shows a dash for a count that failed to load', async () => {
        asAdmin({
            'GET /student-registrations': jsonError(403, 'Access denied'),
            'GET /instructor-registrations': jsonOk([]),
            'GET /semesters': jsonOk([]),
            'GET /course-offerings': jsonOk([]),
        });
        renderApp('/dashboard/admin');

        await waitFor(() => expect(statValue('Student requests pending')).toBe('—'));
        expect(statValue('Instructor requests pending')).toBe('0');
    });
});
