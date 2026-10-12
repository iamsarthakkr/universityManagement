import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { SidebarProvider } from '@/components/ui/base/sidebar';
import { TooltipProvider } from '@/components/ui/base/tooltip';
import { me } from '@/tests/fixtures';
import { Me } from '@/types/auth';
import packageJson from '../../package.json';
import { NavUser } from './NavUser';

function renderNavUser(user: Me) {
    render(
        <TooltipProvider>
            <SidebarProvider>
                <NavUser user={user} onLogout={vi.fn()} />
            </SidebarProvider>
        </TooltipProvider>,
    );
}

describe('NavUser', () => {
    it('shows the app version from package.json in the user menu', async () => {
        renderNavUser(me('ADMIN', { username: 'john.doe' }));

        await userEvent.click(screen.getByRole('button', { name: /john\.doe/ }));

        expect(await screen.findByText(`Version ${packageJson.version}`)).toBeInTheDocument();
    });

    it('shows the profile name and email for students and instructors', async () => {
        renderNavUser(me('STUDENT', { email: 'sam@uni.edu' }));

        await userEvent.click(screen.getByRole('button', { name: /Sam Rao/ }));

        expect(await screen.findByText('sam@uni.edu')).toBeInTheDocument();
    });
});
