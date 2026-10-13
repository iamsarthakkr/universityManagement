import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { renderWithProviders } from '@/tests/render';
import { ActionConfig, ActionMenu } from './ActionMenu';

type Action = 'EDIT' | 'DELETE';

const CONFIG: Record<Action, ActionConfig> = {
    EDIT: { label: 'Edit' },
    DELETE: {
        label: 'Delete',
        destructive: true,
        confirm: { title: (subject) => `Delete ${subject}?`, description: 'Gone for good.', cancelLabel: 'Keep it' },
    },
};

function renderMenu(actions: Action[], onAction = vi.fn()) {
    renderWithProviders(<ActionMenu actions={actions} config={CONFIG} subject="Item 1" onAction={onAction} />);
    return onAction;
}

const openMenu = () => userEvent.click(screen.getByRole('button', { name: 'Actions for Item 1' }));

describe('ActionMenu', () => {
    it('renders nothing when no action is allowed', () => {
        renderMenu([]);

        expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });

    it('lists exactly the given actions and runs one without confirmation', async () => {
        const onAction = renderMenu(['EDIT']);

        await openMenu();
        expect((await screen.findAllByRole('menuitem')).map((item) => item.textContent)).toEqual(['Edit']);
        await userEvent.click(screen.getByRole('menuitem', { name: 'Edit' }));

        expect(onAction).toHaveBeenCalledWith('EDIT');
    });

    it('asks before running an action that needs confirmation', async () => {
        const onAction = renderMenu(['EDIT', 'DELETE']);

        await openMenu();
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Delete' }));
        expect(await screen.findByRole('alertdialog', { name: 'Delete Item 1?' })).toHaveTextContent('Gone for good.');

        await userEvent.click(screen.getByRole('button', { name: 'Keep it' }));
        await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument());
        expect(onAction).not.toHaveBeenCalled();

        await openMenu();
        await userEvent.click(await screen.findByRole('menuitem', { name: 'Delete' }));
        await userEvent.click(await screen.findByRole('button', { name: 'Delete' }));

        expect(onAction).toHaveBeenCalledExactlyOnceWith('DELETE');
    });
});
