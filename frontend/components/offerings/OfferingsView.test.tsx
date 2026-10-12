import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { useAppStore } from '@/stores/appStore';
import { CS, instructor, offering, PHYSICS, semester } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { SemesterStatus, SemesterTerm } from '@/types/semester';
import { OfferingsView } from './OfferingsView';

const SUMMER = semester({ id: 2, term: SemesterTerm.SUMMER, startDate: '2026-04-01' });

function mockRoutes(offerings = [offering()]) {
    useAppStore.setState({ staticData: { departments: [CS, PHYSICS] } });
    return mockFetch({
        'GET /course-offerings': jsonOk(offerings),
        'GET /semesters': jsonOk([semester(), SUMMER]),
        'GET /instructors': jsonOk([instructor()]),
    });
}

const offeringRequests = (requests: { path: string; query: string }[]) =>
    requests.filter((request) => request.path === '/course-offerings').map((request) => request.query);

describe('OfferingsView', () => {
    it('lists offerings with course, semester, instructor and seats', async () => {
        mockRoutes([
            offering({
                semester: {
                    id: 1,
                    term: SemesterTerm.WINTER,
                    year: 2026,
                    status: SemesterStatus.ACTIVE,
                    isRegistrationOpen: true,
                },
            }),
        ]);
        renderWithProviders(<OfferingsView />);

        const row = (await screen.findByText('CS101')).closest('tr') as HTMLElement;
        expect(within(row).getByText('Algorithms')).toBeInTheDocument();
        expect(within(row).getByText('Winter 2026')).toBeInTheDocument();
        expect(within(row).getByText('ACTIVE')).toBeInTheDocument();
        expect(within(row).getByText('Kavya Sen')).toBeInTheDocument();
        expect(within(row).getByText('18 seats left')).toBeInTheDocument();
    });

    it('reads filters from the URL and sends them to the server', async () => {
        const { requests } = mockRoutes();
        renderWithProviders(<OfferingsView />, { path: '/x?semesterId=2&departmentId=1&instructorId=bad' });

        await screen.findByText('CS101');
        expect(offeringRequests(requests)).toEqual(['semesterId=2&departmentId=1']);
        await waitFor(() => expect(screen.getByLabelText('Semester')).toHaveValue('2'));
        expect(screen.getByLabelText('Department')).toHaveValue('1');
        expect(screen.getByLabelText('Instructor')).toHaveValue('');
    });

    it('refetches when a filter changes', async () => {
        const { requests } = mockRoutes();
        renderWithProviders(<OfferingsView />);

        await screen.findByText('CS101');
        await waitFor(() => expect(screen.getByLabelText('Instructor')).toBeEnabled());
        await userEvent.selectOptions(screen.getByLabelText('Instructor'), 'Kavya Sen');
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');

        await waitFor(() =>
            expect(offeringRequests(requests)).toEqual(['', 'instructorId=4', 'departmentId=2&instructorId=4']),
        );
    });

    it('says when filters match nothing', async () => {
        mockRoutes([]);
        renderWithProviders(<OfferingsView />, { path: '/x?departmentId=2' });

        expect(await screen.findByText('No offerings match these filters.')).toBeInTheDocument();
    });

    it('shows the empty state without filters', async () => {
        mockRoutes([]);
        renderWithProviders(<OfferingsView />);

        expect(await screen.findByText('No offerings yet.')).toBeInTheDocument();
    });

    it('keeps the list usable when instructors fail to load', async () => {
        useAppStore.setState({ staticData: { departments: [CS] } });
        mockFetch({
            'GET /course-offerings': jsonOk([offering()]),
            'GET /semesters': jsonOk([]),
            'GET /instructors': jsonError(403, 'Access denied'),
        });
        renderWithProviders(<OfferingsView />);

        expect(await screen.findByText('CS101')).toBeInTheDocument();
        expect(await screen.findByRole('option', { name: 'Instructors unavailable' })).toBeInTheDocument();
    });
});
