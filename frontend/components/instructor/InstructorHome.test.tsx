import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { me, offering } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { SemesterStatus, SemesterTerm } from '@/types/semester';

const OPEN = { id: 1, term: SemesterTerm.WINTER, year: 2026, status: SemesterStatus.PLANNED, isRegistrationOpen: true };
const CLOSED = {
    ...OPEN,
    id: 2,
    term: SemesterTerm.SUMMER,
    status: SemesterStatus.COMPLETED,
    isRegistrationOpen: false,
};

function asInstructor(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([]), 'GET /me': jsonOk(me('INSTRUCTOR')), ...routes });
}

function statValue(label: string) {
    return screen.getByText(label).nextElementSibling?.textContent;
}

describe('InstructorHome', () => {
    it('summarises the offerings and lists those open for registration', async () => {
        asInstructor({
            'GET /me/course-offerings': jsonOk([
                offering({ id: 1, semester: CLOSED, enrolled: 20 }),
                offering({ id: 2, semester: OPEN, enrolled: 5, section: 'B' }),
            ]),
        });
        renderApp('/dashboard/instructor');

        await screen.findByText('Offerings you teach');
        expect(statValue('Offerings you teach')).toBe('2');
        expect(statValue('Students enrolled')).toBe('25');
        expect(within(screen.getByRole('region', { name: 'Open for registration' })).getAllByRole('link')).toHaveLength(
            1,
        );
        expect(screen.getByRole('link', { name: 'CS101 section B' })).toBeInTheDocument();
    });

    it('omits the open-for-registration section when nothing is open', async () => {
        asInstructor({ 'GET /me/course-offerings': jsonOk([offering({ semester: CLOSED })]) });
        renderApp('/dashboard/instructor');

        await screen.findByText('Offerings you teach');
        expect(statValue('Open for registration')).toBe('0');
        expect(screen.queryByRole('region', { name: 'Open for registration' })).not.toBeInTheDocument();
    });

    it('keeps the quick links when the offerings fail to load', async () => {
        asInstructor({ 'GET /me/course-offerings': jsonError(403, 'Access denied') });
        renderApp('/dashboard/instructor');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: /My offerings/ })).toBeInTheDocument();
    });
});
