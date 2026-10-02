'use client';

import * as React from 'react';

import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarRail } from '@/components/ui/base/sidebar';
import { getSidebarNav } from '@/config/navigation/sidebar';
import { useAppActions, useAppStore } from '@/stores/appStore';
import { NavHeader } from './NavHeader';
import { NavMain } from './NavMain';
import { NavUser } from './NavUser';

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const user = useAppStore((state) => state.user);
    const { logout } = useAppActions();

    if (!user) {
        return null;
    }

    return (
        <Sidebar collapsible="icon" {...props}>
            <SidebarHeader>
                <NavHeader />
            </SidebarHeader>
            <SidebarContent>
                <NavMain entries={getSidebarNav(user.role)} />
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={user} onLogout={logout} />
            </SidebarFooter>
            <SidebarRail />
        </Sidebar>
    );
}
