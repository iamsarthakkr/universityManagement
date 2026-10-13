import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { useAppStore } from '@/stores/appStore';
import { CS, PHYSICS, semester } from '@/tests/fixtures';
import { jsonError, jsonOk, mockFetch } from '@/tests/mockFetch';
import { renderWithProviders } from '@/tests/render';
import { SemesterTerm } from '@/types/semester';
import { DepartmentFilter } from './DepartmentFilter';
import { FilterSelect } from './FilterSelect';
import { SemesterFilter } from './SemesterFilter';

describe('FilterSelect', () => {
    const options = [
        { value: 1, label: 'One' },
        { value: 2, label: 'Two' },
    ];

    it('hands back the typed option value, and undefined for "All"', async () => {
        const onChange = vi.fn();
        renderWithProviders(<FilterSelect label="Number" value={1} options={options} onChange={onChange} />);

        await userEvent.selectOptions(screen.getByLabelText('Number'), 'Two');
        expect(onChange).toHaveBeenLastCalledWith(2);

        await userEvent.selectOptions(screen.getByLabelText('Number'), 'All');
        expect(onChange).toHaveBeenLastCalledWith(undefined);
    });
});

describe('FilterSelect with a value that is not an option', () => {
    it('says the filter was not found instead of showing "All"', () => {
        renderWithProviders(
            <FilterSelect label="Number" value={9} options={[{ value: 1, label: 'One' }]} onChange={vi.fn()} />,
        );

        expect(screen.getByLabelText('Number')).toHaveValue('9');
        expect(screen.getByRole('option', { name: 'Not found' })).toBeInTheDocument();
    });

    it('says it is loading while the options are not there yet', () => {
        renderWithProviders(<FilterSelect label="Number" value={9} options={[]} onChange={vi.fn()} disabled />);

        expect(screen.getByRole('option', { name: 'Loading…' })).toBeInTheDocument();
    });
});

describe('DepartmentFilter', () => {
    it('lists the departments loaded at startup', async () => {
        useAppStore.setState({ staticData: { departments: [CS, PHYSICS] } });
        const onChange = vi.fn();
        renderWithProviders(<DepartmentFilter value={undefined} onChange={onChange} />);

        expect(screen.getByLabelText('Department')).toHaveValue('');
        await userEvent.selectOptions(screen.getByLabelText('Department'), 'Physics');

        expect(onChange).toHaveBeenCalledWith(PHYSICS.id);
    });
});

describe('SemesterFilter', () => {
    it('lists semesters newest first', async () => {
        mockFetch({
            'GET /semesters': jsonOk([
                semester({ id: 1, term: SemesterTerm.SUMMER, startDate: '2026-04-01' }),
                semester({ id: 2, term: SemesterTerm.WINTER, startDate: '2026-11-01' }),
            ]),
        });
        renderWithProviders(<SemesterFilter value={2} onChange={vi.fn()} />);

        await waitFor(() => expect(screen.getByLabelText('Semester')).toBeEnabled());
        expect(screen.getAllByRole('option').map((option) => option.textContent)).toEqual([
            'All semesters',
            'Winter 2026',
            'Summer 2026',
        ]);
        expect(screen.getByLabelText('Semester')).toHaveValue('2');
    });

    it('stays disabled and says so when semesters fail to load', async () => {
        mockFetch({ 'GET /semesters': jsonError(403, 'Access denied') });
        renderWithProviders(<SemesterFilter value={undefined} onChange={vi.fn()} />);

        expect(await screen.findByRole('option', { name: 'Semesters unavailable' })).toBeInTheDocument();
        expect(screen.getByLabelText('Semester')).toBeDisabled();
    });
});
