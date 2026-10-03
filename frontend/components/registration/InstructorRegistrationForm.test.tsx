import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { useAppStore } from '@/stores/appStore';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { InstructorRegistrationForm } from './InstructorRegistrationForm';

async function fillForm() {
    await userEvent.type(screen.getByLabelText('Username'), 'prof');
    await userEvent.type(screen.getByLabelText('Password'), 'secret123');
    await userEvent.type(screen.getByLabelText('Email'), 'prof@uni.edu');
    await userEvent.type(screen.getByLabelText('First name'), 'Kavya');
    await userEvent.type(screen.getByLabelText('Last name'), 'Sen');
    await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');
    await userEvent.click(screen.getByRole('button', { name: 'Submit request' }));
}

describe('InstructorRegistrationForm', () => {
    it('submits the registration without a date of birth and clears the form', async () => {
        useAppStore.setState({ staticData: { departments: [{ id: 3, name: 'Physics', code: 'PH' }] } });
        const { requests } = mockFetch({ 'POST /registration/instructor': jsonOk({ id: 1 }) });
        renderWithProviders(<InstructorRegistrationForm />);

        expect(screen.queryByLabelText('Date of birth')).not.toBeInTheDocument();
        await fillForm();

        await waitFor(() => expect(requests).toHaveLength(1));
        expect(requests[0].body).toEqual({
            username: 'prof',
            password: 'secret123',
            email: 'prof@uni.edu',
            firstName: 'Kavya',
            lastName: 'Sen',
            departmentId: 3,
        });
        await waitFor(() => expect(screen.getByLabelText('Username')).toHaveValue(''));
    });

    it('keeps the entered values when the server rejects the request', async () => {
        useAppStore.setState({ staticData: { departments: [{ id: 3, name: 'Physics', code: 'PH' }] } });
        mockFetch({ 'POST /registration/instructor': jsonError(409, 'Username already taken') });
        renderWithProviders(<InstructorRegistrationForm />);

        await fillForm();

        await waitFor(() => expect(screen.getByRole('button', { name: 'Submit request' })).toBeEnabled());
        expect(screen.getByLabelText('Username')).toHaveValue('prof');
    });
});
