import { LucideIcon } from 'lucide-react';

import { Role } from './auth';

export type SidebarNavLink = {
    title: string;
    url: string;
    roles?: Role[];
};

type SidebarNavEntryBase = {
    title: string;
    icon: LucideIcon;
    roles: Role[];
};

export type SidebarNavTopLink = SidebarNavEntryBase & {
    url: string;
};

export type SidebarNavGroup = SidebarNavEntryBase & {
    items: SidebarNavLink[];
};

export type SidebarNavEntry = SidebarNavTopLink | SidebarNavGroup;
