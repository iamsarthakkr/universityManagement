'use client';

import { Link } from 'react-router';
import { SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '@/components/ui/base/sidebar';
import { ICONS, UNIV_SHORT } from '@/config/common';

export function NavHeader() {
    return (
        <SidebarMenu>
            <SidebarMenuItem>
                <SidebarMenuButton
                    size="lg"
                    asChild
                    className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                >
                    <Link to="/dashboard">
                        <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                            <ICONS.mainIcon className="size-6" />
                        </div>
                        <div className="grid flex-1 text-left text-sm leading-tight">
                            <span className="truncate font-medium">{UNIV_SHORT}</span>
                        </div>
                    </Link>
                </SidebarMenuButton>
            </SidebarMenuItem>
        </SidebarMenu>
    );
}
