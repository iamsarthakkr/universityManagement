import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { enrollment, me, semester } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { EnrollmentStatus } from '@/types/enrollment';

function asStudent(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([]), 'GET /me': jsonOk(me('STUDENT')), ...routes });
}

const statValue = (label: string) => screen.getByText(label).nextElementSibling?.textContent;

describe('StudentHome', () => {
    it('announces open registration and counts enrollments', async () => {
        asStudent({
            'GET /semesters': jsonOk([semester({ isRegistrationOpen: true }), semester({ id: 2, year: 2025 })]),
            'GET /me/enrollments': jsonOk([
                enrollment({ id: 1, status: EnrollmentStatus.ENROLLED }),
                enrollment({ id: 2, status: EnrollmentStatus.PENDING }),
                enrollment({ id: 3, status: EnrollmentStatus.PENDING }),
                enrollment({ id: 4, status: EnrollmentStatus.REJECTED }),
            ]),
        });
        renderApp('/dashboard/student');

        const banner = await screen.findByRole('region', { name: 'Registration open' });
        expect(banner).toHaveTextContent('Registration for Winter 2026 is open until 15 Oct 2026.');
        expect(within(banner).getByRole('link', { name: 'Browse offerings' })).toBeInTheDocument();
        expect(statValue('Courses enrolled')).toBe('1');
        expect(statValue('Requests awaiting approval')).toBe('2');
    });

    it('has no banner while registration is closed', async () => {
        asStudent({ 'GET /semesters': jsonOk([semester()]), 'GET /me/enrollments': jsonOk([]) });
        renderApp('/dashboard/student');

        await screen.findByText('Courses enrolled');
        expect(screen.queryByRole('region', { name: 'Registration open' })).not.toBeInTheDocument();
    });

    it('keeps the quick links when enrollments fail to load', async () => {
        asStudent({ 'GET /semesters': jsonOk([]), 'GET /me/enrollments': jsonError(403, 'Access denied') });
        renderApp('/dashboard/student');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: /Enroll in courses/ })).toBeInTheDocument();
    });
});
