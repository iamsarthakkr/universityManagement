import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { CourseCatalogue } from './CourseCatalogue';

const CATALOGUE = [
    {
        departmentId: 1,
        departmentName: 'Computer Science',
        courseList: [
            { courseId: 10, departmentId: 1, code: 'CS101', title: 'Algorithms', description: 'Sorting', credits: 4 },
            { courseId: 11, departmentId: 1, code: 'CS102', title: 'Databases', description: 'SQL', credits: 3 },
        ],
    },
    {
        departmentId: 2,
        departmentName: 'Physics',
        courseList: [
            { courseId: 20, departmentId: 2, code: 'PH101', title: 'Mechanics', description: 'Motion', credits: 1 },
        ],
    },
];

describe('CourseCatalogue', () => {
    it('lists departments with their course counts', async () => {
        mockFetch({ 'GET /courses/catalogue': jsonOk(CATALOGUE) });
        renderWithProviders(<CourseCatalogue />);

        expect(await screen.findByText('Computer Science')).toBeInTheDocument();
        expect(screen.getByText('2 courses')).toBeInTheDocument();
        expect(screen.getByText('1 course')).toBeInTheDocument();
    });

    it('expands a department and shows course details on demand', async () => {
        mockFetch({ 'GET /courses/catalogue': jsonOk(CATALOGUE) });
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
        mockFetch({ 'GET /courses/catalogue': jsonOk([]) });
        renderWithProviders(<CourseCatalogue />);

        expect(await screen.findByText('No courses available yet.')).toBeInTheDocument();
    });

    it('shows the server error', async () => {
        mockFetch({ 'GET /courses/catalogue': jsonError(403, 'Access denied') });
        renderWithProviders(<CourseCatalogue />);

        expect(await screen.findByText('Access denied')).toBeInTheDocument();
    });
});
