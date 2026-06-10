'use client';

import * as React from 'react';

import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarRail } from '@/components/ui/base/sidebar';
import { NAV_MAIN } from '@/config/navigation/sidebar';
import { useAuth } from '@/context/AuthContext';
import { NavHeader } from './NavHeader';
import { NavMain } from './NavMain';
import { NavUser } from './NavUser';

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const auth = useAuth();
    if (!auth.user) {
        return null;
    }

    const navItems = NAV_MAIN.filter((item) => auth.user && item.roles.includes(auth.user?.role));
    return (
        <Sidebar collapsible="icon" {...props}>
            <SidebarHeader>
                <NavHeader />
            </SidebarHeader>
            <SidebarContent>
                <NavMain items={navItems} />
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={auth.user} onLogout={auth.logout} />
            </SidebarFooter>
            <SidebarRail />
        </Sidebar>
    );
}
