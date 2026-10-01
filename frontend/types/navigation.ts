import { LucideIcon } from 'lucide-react';

import { Role } from './auth';

export type SidebarNavLink = {
    title: string;
    url: string;
    roles?: Role[];
};

export type SidebarNavGroup = {
    title: string;
    icon: LucideIcon;
    roles: Role[];
    items: SidebarNavLink[];
};
