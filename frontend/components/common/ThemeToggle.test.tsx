import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { useThemeStore } from '@/stores/themeStore';
import { ThemeToggle } from './ThemeToggle';

describe('ThemeToggle', () => {
    it('switches the theme from the menu and marks the active option', async () => {
        render(<ThemeToggle />);

        await userEvent.click(screen.getByRole('button', { name: 'Change theme' }));
        await userEvent.click(await screen.findByRole('menuitemradio', { name: 'Dark' }));

        expect(useThemeStore.getState().theme).toBe('dark');
        expect(document.documentElement).toHaveClass('dark');

        await userEvent.click(screen.getByRole('button', { name: 'Change theme' }));
        expect(await screen.findByRole('menuitemradio', { name: 'Dark' })).toHaveAttribute('aria-checked', 'true');
    });
});
