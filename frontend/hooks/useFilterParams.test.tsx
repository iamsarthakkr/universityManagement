import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useLocation } from 'react-router';
import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/tests/render';
import { EnrollmentStatus } from '@/types/enrollment';
import { enumParam, idParam, textParam, useFilterParams } from './useFilterParams';

const SCHEMA = {
    semesterId: idParam,
    status: enumParam(Object.values(EnrollmentStatus)),
};

function Probe() {
    const [filters, setFilters] = useFilterParams(SCHEMA);
    const location = useLocation();

    return (
        <>
            <output aria-label="filters">{JSON.stringify(filters)}</output>
            <output aria-label="search">{location.search}</output>
            <button onClick={() => setFilters({ semesterId: 4 })}>Pick semester</button>
            <button onClick={() => setFilters({ status: undefined })}>Clear status</button>
        </>
    );
}

const filters = () => JSON.parse(screen.getByLabelText('filters').textContent ?? '{}');
const search = () => screen.getByLabelText('search').textContent;

describe('useFilterParams', () => {
    it('reads typed filters from the URL', () => {
        renderWithProviders(<Probe />, { path: '/x?semesterId=3&status=pending&other=1' });

        expect(filters()).toEqual({ semesterId: 3, status: 'PENDING' });
    });

    it.each(['semesterId=abc', 'semesterId=0', 'semesterId=1.5', 'status=foo'])(
        'drops the invalid value in %s',
        (query) => {
            renderWithProviders(<Probe />, { path: `/x?${query}` });

            expect(filters()).toEqual({});
        },
    );

    it('sets and clears filters without touching unrelated params', async () => {
        renderWithProviders(<Probe />, { path: '/x?status=ENROLLED&tab=2' });

        await userEvent.click(screen.getByRole('button', { name: 'Pick semester' }));
        expect(filters()).toEqual({ semesterId: 4, status: 'ENROLLED' });

        await userEvent.click(screen.getByRole('button', { name: 'Clear status' }));
        expect(filters()).toEqual({ semesterId: 4 });
        expect(search()).toBe('?tab=2&semesterId=4');
    });
});

describe('textParam', () => {
    it('keeps the text as typed and drops blank values', () => {
        expect(textParam('data ')).toBe('data ');
        expect(textParam('   ')).toBeUndefined();
    });
});
