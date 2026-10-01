'use client';

import * as React from 'react';

import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarRail } from '@/components/ui/base/sidebar';
import { getSidebarNav } from '@/config/navigation/sidebar';
import { useAuth } from '@/context/AuthContext';
import { NavHeader } from './NavHeader';
import { NavMain } from './NavMain';
import { NavUser } from './NavUser';

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const auth = useAuth();
    if (!auth.user) {
        return null;
    }

    return (
        <Sidebar collapsible="icon" {...props}>
            <SidebarHeader>
                <NavHeader />
            </SidebarHeader>
            <SidebarContent>
                <NavMain entries={getSidebarNav(auth.user.role)} />
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={auth.user} onLogout={auth.logout} />
            </SidebarFooter>
            <SidebarRail />
        </Sidebar>
    );
}
