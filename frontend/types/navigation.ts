import { LucideIcon } from 'lucide-react';

import { Role } from './auth';

export type SidebarNavSubItem = {
    title: string;
    url: string;
    roles?: Array<Role>;
};

export type SidebarNavItem = {
    title: string;
    url: string;
    icon: LucideIcon;
    open?: boolean;
    roles: Array<Role>;
    items?: SidebarNavSubItem[];
};
