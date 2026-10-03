import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { SidebarProvider } from '@/components/ui/base/sidebar';
import { TooltipProvider } from '@/components/ui/base/tooltip';
import packageJson from '../../package.json';
import { NavUser } from './NavUser';

const USER = { id: '1', username: 'john.doe', email: 'john@uni.edu', role: 'ADMIN' as const };

function renderNavUser() {
    render(
        <TooltipProvider>
            <SidebarProvider>
                <NavUser user={USER} onLogout={vi.fn()} />
            </SidebarProvider>
        </TooltipProvider>,
    );
}

describe('NavUser', () => {
    it('shows the app version from package.json in the user menu', async () => {
        renderNavUser();

        await userEvent.click(screen.getByRole('button', { name: /john\.doe/ }));

        expect(await screen.findByText(`Version ${packageJson.version}`)).toBeInTheDocument();
    });
});
