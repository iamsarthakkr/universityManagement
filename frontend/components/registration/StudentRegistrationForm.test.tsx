import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { toLocalIsoDate } from '@/lib/date';
import { useAppStore } from '@/stores/appStore';
import { jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { StudentRegistrationForm } from './StudentRegistrationForm';

describe('StudentRegistrationForm', () => {
    it('does not allow a future date of birth', () => {
        renderWithProviders(<StudentRegistrationForm />);

        expect(screen.getByLabelText('Date of birth')).toHaveAttribute('max', toLocalIsoDate(new Date()));
    });

    it('submits the registration and clears the form', async () => {
        useAppStore.setState({ staticData: { departments: [{ id: 3, name: 'Physics', code: 'PH' }] } });
        const { requests } = mockFetch({ 'POST /registration/student': jsonOk({ id: 1 }) });
        renderWithProviders(<StudentRegistrationForm />);

        await userEvent.type(screen.getByLabelText('Username'), 'ana');
        await userEvent.type(screen.getByLabelText('Password'), 'secret123');
        await userEvent.type(screen.getByLabelText('Email'), 'ana@uni.edu');
        await userEvent.type(screen.getByLabelText('First name'), 'Ana');
        await userEvent.type(screen.getByLabelText('Date of birth'), '2005-02-03');
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');
        await userEvent.click(screen.getByRole('button', { name: 'Submit request' }));

        await waitFor(() => expect(requests).toHaveLength(1));
        expect(requests[0].headers.has('Authorization')).toBe(false);
        expect(requests[0].body).toEqual({
            username: 'ana',
            password: 'secret123',
            email: 'ana@uni.edu',
            firstName: 'Ana',
            lastName: '',
            dateOfBirth: '2005-02-03',
            departmentId: 3,
        });
        await waitFor(() => expect(screen.getByLabelText('Username')).toHaveValue(''));
    });
});
