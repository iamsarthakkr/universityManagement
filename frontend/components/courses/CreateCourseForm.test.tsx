import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';

import { useAppStore } from '@/stores/appStore';
import { jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { CreateCourseForm } from './CreateCourseForm';

beforeEach(() => {
    useAppStore.setState({
        staticData: {
            departments: [
                { id: 1, name: 'Computer Science', code: 'CS' },
                { id: 2, name: 'Business Administration', code: 'MBA' },
            ],
        },
    });
});

describe('CreateCourseForm', () => {
    it('keeps the course code disabled until a department is chosen', async () => {
        renderWithProviders(<CreateCourseForm />);

        expect(screen.getByLabelText('Course Code')).toBeDisabled();

        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Computer Science');

        expect(screen.getByLabelText('Course Code')).toBeEnabled();
    });

    it('builds the code without doubling the prefix and submits the request', async () => {
        const { requests } = mockFetch({ 'POST /courses': jsonOk({ courseId: 10 }) });
        renderWithProviders(<CreateCourseForm />);

        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Computer Science');
        await userEvent.type(screen.getByLabelText('Course Code'), 'cs101');
        expect(screen.getByText('Saved as CS101')).toBeInTheDocument();

        await userEvent.type(screen.getByLabelText('Title'), 'Algorithms');
        await userEvent.type(screen.getByLabelText('Description'), 'Sorting and searching');
        await userEvent.clear(screen.getByLabelText('Credits (1–10)'));
        await userEvent.type(screen.getByLabelText('Credits (1–10)'), '4');
        await userEvent.click(screen.getByRole('button', { name: 'Create course' }));

        await waitFor(() => expect(requests).toHaveLength(1));
        expect(requests[0].body).toEqual({
            title: 'Algorithms',
            description: 'Sorting and searching',
            credits: 4,
            departmentId: 1,
            code: 'CS101',
        });
        await waitFor(() => expect(screen.getByLabelText('Title')).toHaveValue(''));
    });
});
