import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { course, CS, instructor, me, offering, PHYSICS, semester } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { SemesterStatus, SemesterTerm } from '@/types/semester';

const WINTER = semester({ id: 1 });
const SUMMER = semester({ id: 2, term: SemesterTerm.SUMMER, year: 2027, startDate: '2027-04-01' });
const ACTIVE = semester({
    id: 3,
    year: 2025,
    startDate: '2025-11-01',
    status: SemesterStatus.ACTIVE,
    acceptsOfferings: false,
});

function asAdmin(routes: Parameters<typeof mockFetch>[0] = {}) {
    setToken('valid');
    return mockFetch({
        'GET /departments': jsonOk([CS, PHYSICS]),
        'GET /me': jsonOk(me('ADMIN')),
        'GET /semesters': jsonOk([WINTER, SUMMER, ACTIVE]),
        'GET /courses': (request) => jsonOk(request.query === 'departmentId=1' ? [course()] : []),
        'GET /instructors': (request) => jsonOk(request.query === 'departmentId=1' ? [instructor()] : []),
        ...routes,
    });
}

async function fillForm() {
    await waitFor(() => expect(screen.getByLabelText('Semester')).toBeEnabled());
    await userEvent.selectOptions(screen.getByLabelText('Semester'), 'Winter 2026');
    await userEvent.selectOptions(screen.getByLabelText('Department'), 'Computer Science');
    await waitFor(() => expect(screen.getByLabelText('Course')).toBeEnabled());
    await userEvent.selectOptions(screen.getByLabelText('Course'), 'CS101 · Algorithms');
    await waitFor(() => expect(screen.getByLabelText('Instructor')).toBeEnabled());
    await userEvent.selectOptions(screen.getByLabelText('Instructor'), 'Kavya Sen');
    await userEvent.type(screen.getByLabelText('Section'), ' b ');
    await userEvent.clear(screen.getByLabelText('Capacity'));
    await userEvent.type(screen.getByLabelText('Capacity'), '25');
}

describe('CreateOfferingForm', () => {
    it('lists only semesters that accept offerings, newest first', async () => {
        asAdmin();
        renderApp('/dashboard/admin/offerings/new');

        await waitFor(() => expect(screen.getByLabelText('Semester')).toBeEnabled());
        expect(
            Array.from((screen.getByLabelText('Semester') as HTMLSelectElement).options).map((option) => option.text),
        ).toEqual(['Select a semester', 'Summer 2027', 'Winter 2026']);
    });

    it('keeps course and instructor locked until a department is chosen, then loads that department', async () => {
        const { requests } = asAdmin();
        renderApp('/dashboard/admin/offerings/new');

        expect(await screen.findByLabelText('Course')).toBeDisabled();
        expect(screen.getAllByRole('option', { name: 'Select a department first', hidden: true })).toHaveLength(2);

        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');

        expect(await screen.findByText('This department has no courses yet.')).toBeInTheDocument();
        expect(screen.getByText('This department has no instructors yet.')).toBeInTheDocument();
        expect(requests.filter((request) => request.path === '/courses').map((request) => request.query)).toEqual([
            'departmentId=2',
        ]);
    });

    it('clears the chosen course and instructor when the department changes', async () => {
        asAdmin();
        renderApp('/dashboard/admin/offerings/new');

        await fillForm();
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');

        expect(screen.getByLabelText('Course')).toHaveValue('');
        expect(screen.getByLabelText('Instructor')).toHaveValue('');
    });

    it('creates the offering and opens its semester', async () => {
        const { requests } = asAdmin({
            'POST /course-offerings': jsonOk(offering({ section: 'B' })),
            'GET /semesters/1': jsonOk(WINTER),
            'GET /course-offerings': jsonOk([offering({ section: 'B' })]),
        });
        const router = renderApp('/dashboard/admin/offerings/new');

        await fillForm();
        await userEvent.click(screen.getByRole('button', { name: 'Create offering' }));

        expect(await screen.findByText('CS101 section B added.')).toBeInTheDocument();
        await waitFor(() => expect(router.state.location.pathname).toBe('/dashboard/admin/semesters/1'));
        expect(requests.find((request) => request.method === 'POST')?.body).toEqual({
            semesterId: 1,
            courseId: 10,
            instructorId: 4,
            section: 'B',
            capacity: 25,
        });
    });

    it('keeps "Create offering" disabled until semester, course and instructor are chosen', async () => {
        asAdmin({ 'GET /courses': jsonError(500, 'Server error') });
        renderApp('/dashboard/admin/offerings/new');

        await waitFor(() => expect(screen.getByLabelText('Semester')).toBeEnabled());
        await userEvent.selectOptions(screen.getByLabelText('Semester'), 'Winter 2026');
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Computer Science');
        await waitFor(() => expect(screen.getByLabelText('Instructor')).toBeEnabled());
        await userEvent.selectOptions(screen.getByLabelText('Instructor'), 'Kavya Sen');

        expect(screen.getByLabelText('Course')).toBeDisabled();
        expect(screen.getByRole('button', { name: 'Create offering' })).toBeDisabled();
    });

    it('preselects the semester from the link', async () => {
        asAdmin();
        renderApp('/dashboard/admin/offerings/new?semesterId=2');
        await waitFor(() => expect(screen.getByLabelText('Semester')).toHaveValue('2'));
    });

    it('ignores a preselected semester that does not accept offerings', async () => {
        asAdmin();
        renderApp('/dashboard/admin/offerings/new?semesterId=3');

        await waitFor(() => expect(screen.getByLabelText('Semester')).toBeEnabled());
        expect(screen.getByLabelText('Semester')).toHaveValue('');
    });

    it('points to semester creation when no semester accepts offerings', async () => {
        asAdmin({ 'GET /semesters': jsonOk([ACTIVE]) });
        renderApp('/dashboard/admin/offerings/new');

        expect(await screen.findByText(/No semester is accepting offerings right now/)).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Create a semester' })).toHaveAttribute(
            'href',
            '/dashboard/admin/semesters/new',
        );
    });

    it('keeps the form and shows the server message when creation fails', async () => {
        asAdmin({ 'POST /course-offerings': jsonError(409, 'course offering already exists') });
        const router = renderApp('/dashboard/admin/offerings/new');

        await fillForm();
        await userEvent.click(screen.getByRole('button', { name: 'Create offering' }));

        expect(await screen.findByText('Failed to create offering')).toBeInTheDocument();
        expect(screen.getByText('course offering already exists')).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/dashboard/admin/offerings/new');
        expect(screen.getByLabelText('Section')).toHaveValue(' b ');
    });

    it('shows why semesters are missing when they fail to load', async () => {
        asAdmin({ 'GET /semesters': jsonError(403, 'Access denied') });
        renderApp('/dashboard/admin/offerings/new');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
        expect(screen.getByLabelText('Semester')).toBeDisabled();
    });
});
