import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { CS, enrollment, me, offering } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { EnrollmentStatus } from '@/types/enrollment';
import { OfferingAction } from '@/types/offering';
import { SemesterStatus, SemesterTerm } from '@/types/semester';

const OPEN = { id: 1, term: SemesterTerm.WINTER, year: 2026, status: SemesterStatus.PLANNED, isRegistrationOpen: true };
const PAST = {
    id: 2,
    term: SemesterTerm.SUMMER,
    year: 2026,
    status: SemesterStatus.COMPLETED,
    isRegistrationOpen: false,
};

const OPEN_OFFERING = offering({ id: 9, semester: OPEN, allowedActions: [OfferingAction.ENROLL] });
const PAST_OFFERING = offering({
    id: 20,
    semester: PAST,
    course: { id: 11, code: 'CS102', title: 'Databases', credits: 3 },
});

function asStudent(routes: Parameters<typeof mockFetch>[0]) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk([CS]),
        'GET /me': jsonOk(me('STUDENT')),
        'GET /semesters': jsonOk([]),
        'GET /course-offerings': jsonOk([PAST_OFFERING, OPEN_OFFERING]),
        'GET /me/enrollments': jsonOk([]),
        ...routes,
    });
}

function renderAppWith(path: string) {
    asStudent({});
    return renderApp(path);
}

const card = (name: string) => screen.getByRole('article', { name });

describe('BrowseOfferingsView', () => {
    it('shows only offerings open for registration by default', async () => {
        asStudent({});
        renderApp('/dashboard/student/offerings');

        expect(await screen.findByRole('article', { name: 'CS101 section A' })).toBeInTheDocument();
        expect(screen.queryByRole('article', { name: 'CS102 section A' })).not.toBeInTheDocument();
        expect(within(card('CS101 section A')).getByText('Kavya Sen · 4 credits')).toBeInTheDocument();
    });

    it('shows every semester, latest first, on the All tab', async () => {
        const router = renderAppWith('/dashboard/student/offerings');

        await screen.findByRole('article', { name: 'CS101 section A' });
        await userEvent.click(screen.getByRole('tab', { name: 'All semesters' }));

        expect(await screen.findByRole('article', { name: 'CS102 section A' })).toBeInTheDocument();
        expect(
            screen.getAllByRole('region', { name: /\d{4}$/ }).map((region) => region.getAttribute('aria-label')),
        ).toEqual(['Winter 2026', 'Summer 2026']);
        expect(router.state.location.search).toBe('?view=ALL');
    });

    it('offers "Request enrollment" only where the server allows it', async () => {
        renderAppWith('/dashboard/student/offerings?view=all');

        await screen.findByRole('article', { name: 'CS102 section A' });
        expect(within(card('CS101 section A')).getByRole('button', { name: 'Request enrollment' })).toBeInTheDocument();
        expect(within(card('CS102 section A')).queryByRole('button')).not.toBeInTheDocument();
    });

    it('requests enrollment and shows the new status', async () => {
        let requested = false;
        const { requests } = asStudent({
            'GET /me/enrollments': () => jsonOk(requested ? [enrollment()] : []),
            'POST /course-offerings/9/enrollments': () => {
                requested = true;
                return jsonOk(enrollment());
            },
        });
        renderApp('/dashboard/student/offerings');

        await userEvent.click(await screen.findByRole('button', { name: 'Request enrollment' }));

        expect(await screen.findByText("Requested CS101. You'll see it under Enrollments.")).toBeInTheDocument();
        expect(await within(card('CS101 section A')).findByText(EnrollmentStatus.PENDING)).toBeInTheDocument();
        expect(requests.filter((request) => request.method === 'POST').map((request) => request.path)).toEqual([
            '/course-offerings/9/enrollments',
        ]);
    });

    it('shows the server message when the request is refused', async () => {
        asStudent({ 'POST /course-offerings/9/enrollments': jsonError(409, 'Course offering is full') });
        renderApp('/dashboard/student/offerings');

        await userEvent.click(await screen.findByRole('button', { name: 'Request enrollment' }));

        expect(await screen.findByText('Request failed')).toBeInTheDocument();
        expect(screen.getByText('Course offering is full')).toBeInTheDocument();
    });

    it('sends the semester and department filters, but not the tab, to the server', async () => {
        const { requests } = asStudent({});
        renderApp('/dashboard/student/offerings?view=all&departmentId=1&semesterId=2');

        await screen.findByRole('article', { name: 'CS102 section A' });
        expect(
            requests.filter((request) => request.path === '/course-offerings').map((request) => request.query),
        ).toEqual(['semesterId=2&departmentId=1']);
    });

    it('says when the student’s own requests could not be loaded', async () => {
        asStudent({ 'GET /me/enrollments': jsonError(403, 'Access denied') });
        renderApp('/dashboard/student/offerings');

        expect(await screen.findByRole('article', { name: 'CS101 section A' })).toBeInTheDocument();
        expect(await screen.findByRole('alert')).toHaveTextContent("Your existing requests couldn't be loaded");
    });

    it('says when nothing is open for registration', async () => {
        asStudent({ 'GET /course-offerings': jsonOk([PAST_OFFERING]) });
        renderApp('/dashboard/student/offerings');

        expect(await screen.findByText('No offerings are open for registration right now.')).toBeInTheDocument();
    });

    it('says when filters match nothing on the All tab', async () => {
        asStudent({ 'GET /course-offerings': jsonOk([]) });
        renderApp('/dashboard/student/offerings?view=all');

        expect(await screen.findByText('No offerings match these filters.')).toBeInTheDocument();
    });
});
