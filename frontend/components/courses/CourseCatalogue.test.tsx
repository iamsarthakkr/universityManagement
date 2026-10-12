import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { course, PHYSICS } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { CourseCatalogue } from './CourseCatalogue';

const COURSES = [
    course({ id: 20, department: PHYSICS, code: 'PH101', title: 'Mechanics', description: 'Motion', credits: 1 }),
    course({ id: 10, code: 'CS101', title: 'Algorithms', description: 'Sorting', credits: 4 }),
    course({ id: 11, code: 'CS102', title: 'Databases', description: 'SQL', credits: 3 }),
];

describe('CourseCatalogue', () => {
    it('groups the courses by department, departments in name order', async () => {
        mockFetch({ 'GET /courses': jsonOk(COURSES) });
        renderWithProviders(<CourseCatalogue />);

        expect(await screen.findByText('Computer Science')).toBeInTheDocument();
        expect(screen.getAllByRole('heading', { level: 3 }).map((heading) => heading.textContent)).toEqual([
            'Computer Science',
            'Physics',
        ]);
        expect(screen.getByText('2 courses')).toBeInTheDocument();
        expect(screen.getByText('1 course')).toBeInTheDocument();
    });

    it('expands a department and shows course details on demand', async () => {
        mockFetch({ 'GET /courses': jsonOk(COURSES) });
        renderWithProviders(<CourseCatalogue />);

        await userEvent.click(await screen.findByText('Computer Science'));
        expect(screen.getByText('Algorithms')).toBeInTheDocument();
        expect(screen.queryByText('Sorting')).not.toBeInTheDocument();

        const [showMore] = screen.getAllByRole('button', { name: /show more/i });
        await userEvent.click(showMore);

        expect(screen.getByText('Sorting')).toBeInTheDocument();
        expect(screen.getByText('4')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /show less/i })).toBeInTheDocument();
    });

    it('shows the empty state when there are no courses', async () => {
        mockFetch({ 'GET /courses': jsonOk([]) });
        renderWithProviders(<CourseCatalogue />);

        expect(await screen.findByText('No courses available yet.')).toBeInTheDocument();
    });

    it('shows the server error', async () => {
        mockFetch({ 'GET /courses': jsonError(403, 'Access denied') });
        renderWithProviders(<CourseCatalogue />);

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
    });
});
