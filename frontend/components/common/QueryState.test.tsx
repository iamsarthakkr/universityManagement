import { UseQueryResult } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { QueryState } from './QueryState';

function queryResult(overrides: Partial<UseQueryResult<unknown, Error>>) {
    return {
        isPending: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
        ...overrides,
    } as unknown as UseQueryResult<unknown, Error>;
}

function renderState(query: UseQueryResult<unknown, Error>, isEmpty = false) {
    return render(
        <QueryState query={query} isEmpty={isEmpty} emptyMessage="Nothing here.">
            <p>content</p>
        </QueryState>,
    );
}

describe('QueryState', () => {
    it('shows a spinner while loading', () => {
        renderState(queryResult({ isPending: true }));

        expect(screen.getByRole('status', { name: 'Loading' })).toBeInTheDocument();
        expect(screen.queryByText('content')).not.toBeInTheDocument();
    });

    it('shows the error message and retries on click', async () => {
        const query = queryResult({ isError: true, error: new Error('Server error') });
        renderState(query);

        expect(screen.getByText('Server error')).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', { name: 'Try again' }));

        expect(query.refetch).toHaveBeenCalledTimes(1);
    });

    it('shows the empty message when there is no data', () => {
        renderState(queryResult({}), true);

        expect(screen.getByText('Nothing here.')).toBeInTheDocument();
        expect(screen.queryByText('content')).not.toBeInTheDocument();
    });

    it('renders children when data is available', () => {
        renderState(queryResult({}));

        expect(screen.getByText('content')).toBeInTheDocument();
    });
});
