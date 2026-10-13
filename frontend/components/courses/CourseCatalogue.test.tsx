import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { setToken } from '@/lib/session';
import { course, CS, me, PHYSICS } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderApp } from '@/tests/render';
import { Role } from '@/types/auth';

const COURSES = [
    course({ id: 20, department: PHYSICS, code: 'PH101', title: 'Mechanics', description: 'Motion', credits: 1 }),
    course({ id: 10, code: 'CS101', title: 'Algorithms', description: 'Sorting', credits: 4 }),
    course({ id: 11, code: 'CS102', title: 'Databases', description: 'SQL', credits: 3 }),
];

function as(role: Role, routes: Parameters<typeof mockFetch>[0] = { 'GET /courses': jsonOk(COURSES) }) {
    setToken('valid');
    return mockFetch({ 'GET /departments': jsonOk([CS, PHYSICS]), 'GET /me': jsonOk(me(role)), ...routes });
}

const findDepartment = (name: string) => screen.findByRole('heading', { level: 3, name });

const departmentHeadings = () => screen.getAllByRole('heading', { level: 3 }).map((heading) => heading.textContent);

describe('CourseCatalogue', () => {
    it('groups the courses by department, departments in name order, collapsed', async () => {
        as('STUDENT');
        renderApp('/dashboard/courses');

        expect(await findDepartment('Computer Science')).toBeInTheDocument();
        expect(departmentHeadings()).toEqual(['Computer Science', 'Physics']);
        expect(screen.getByText('2 courses')).toBeInTheDocument();
        expect(screen.getByText('1 course')).toBeInTheDocument();
        expect(screen.queryByText('Algorithms')).not.toBeInTheDocument();
    });

    it('shows credits up front and the description on demand', async () => {
        as('STUDENT');
        renderApp('/dashboard/courses');

        await userEvent.click(await findDepartment('Computer Science'));
        expect(screen.getByText('CS101 · 4 credits')).toBeInTheDocument();
        expect(screen.queryByText('Sorting')).not.toBeInTheDocument();

        const [showMore] = screen.getAllByRole('button', { name: /show more/i });
        await userEvent.click(showMore);

        expect(screen.getByText('Sorting')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /show less/i })).toHaveAttribute('aria-expanded', 'true');
    });

    it('searches code and title, opening the matching groups', async () => {
        as('STUDENT');
        const router = renderApp('/dashboard/courses');

        await findDepartment('Computer Science');
        await userEvent.type(screen.getByLabelText('Search'), 'data');

        expect(await screen.findByText('Databases')).toBeInTheDocument();
        expect(screen.queryByText('Algorithms')).not.toBeInTheDocument();
        expect(departmentHeadings()).toEqual(['Computer Science']);
        expect(router.state.location.search).toBe('?q=data');

        await userEvent.clear(screen.getByLabelText('Search'));
        await waitFor(() => expect(departmentHeadings()).toEqual(['Computer Science', 'Physics']));
        expect(router.state.location.search).toBe('');
    });

    it('keeps spaces typed between words', async () => {
        as('STUDENT');
        renderApp('/dashboard/courses');

        await findDepartment('Computer Science');
        await userEvent.type(screen.getByLabelText('Search'), 'cs 1');

        expect(screen.getByLabelText('Search')).toHaveValue('cs 1');
    });

    it('asks the server for one department', async () => {
        const { requests } = as('STUDENT');
        renderApp('/dashboard/courses');

        await findDepartment('Computer Science');
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');

        await waitFor(() =>
            expect(requests.filter((request) => request.path === '/courses').map((request) => request.query)).toEqual([
                '',
                'departmentId=2',
            ]),
        );
    });

    it('says when nothing matches', async () => {
        as('STUDENT');
        renderApp('/dashboard/courses?q=zzz');

        expect(await screen.findByText('No courses match your search.')).toBeInTheDocument();
    });

    it('shows the empty state when there are no courses', async () => {
        as('STUDENT', { 'GET /courses': jsonOk([]) });
        renderApp('/dashboard/courses');

        expect(await screen.findByText('No courses available yet.')).toBeInTheDocument();
    });

    it('shows the server error', async () => {
        as('STUDENT', { 'GET /courses': jsonError(403, 'Access denied') });
        renderApp('/dashboard/courses');

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
    });

    it('offers "New course" to admins only', async () => {
        as('ADMIN');
        renderApp('/dashboard/courses');

        expect(await screen.findByRole('link', { name: 'New course' })).toHaveAttribute(
            'href',
            '/dashboard/courses/new',
        );
    });

    it('hides "New course" from students', async () => {
        as('STUDENT');
        renderApp('/dashboard/courses');

        await findDepartment('Computer Science');
        expect(screen.queryByRole('link', { name: 'New course' })).not.toBeInTheDocument();
    });
});
