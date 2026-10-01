'use client';

import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/base/collapsible';
import {
    SidebarGroup,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarMenuSub,
    SidebarMenuSubButton,
    SidebarMenuSubItem,
} from '@/components/ui/base/sidebar';
import { SidebarNavEntry } from '@/types/navigation';
import { ChevronRightIcon } from 'lucide-react';
import { Link, useLocation } from 'react-router';

interface Props {
    entries: SidebarNavEntry[];
}

export function NavMain(props: Props) {
    const { entries } = props;
    const { pathname } = useLocation();

    return (
        <SidebarGroup>
            <SidebarGroupLabel>Platform</SidebarGroupLabel>
            <SidebarMenu>
                {entries.map((entry) => {
                    const Icon = entry.icon;

                    if (!('items' in entry)) {
                        return (
                            <SidebarMenuItem key={entry.url}>
                                <SidebarMenuButton asChild tooltip={entry.title} isActive={pathname === entry.url}>
                                    <Link to={entry.url}>
                                        <Icon />
                                        <span className="text-sm">{entry.title}</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        );
                    }

                    return (
                        <Collapsible key={entry.title} asChild defaultOpen className="group/collapsible">
                            <SidebarMenuItem>
                                <CollapsibleTrigger asChild>
                                    <SidebarMenuButton tooltip={entry.title}>
                                        <Icon />
                                        <span className="text-sm">{entry.title}</span>
                                        <ChevronRightIcon className="ml-auto transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90" />
                                    </SidebarMenuButton>
                                </CollapsibleTrigger>
                                <CollapsibleContent>
                                    <SidebarMenuSub>
                                        {entry.items.map((link) => (
                                            <SidebarMenuSubItem key={link.url}>
                                                <SidebarMenuSubButton asChild isActive={pathname === link.url}>
                                                    <Link to={link.url}>
                                                        <span className="text-xs">{link.title}</span>
                                                    </Link>
                                                </SidebarMenuSubButton>
                                            </SidebarMenuSubItem>
                                        ))}
                                    </SidebarMenuSub>
                                </CollapsibleContent>
                            </SidebarMenuItem>
                        </Collapsible>
                    );
                })}
            </SidebarMenu>
        </SidebarGroup>
    );
}
