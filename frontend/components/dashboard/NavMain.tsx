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
import { SidebarNavGroup } from '@/types/navigation';
import { ChevronRightIcon } from 'lucide-react';
import { Link, useLocation } from 'react-router';

interface Props {
    groups: SidebarNavGroup[];
}

export function NavMain(props: Props) {
    const { groups } = props;
    const { pathname } = useLocation();

    return (
        <SidebarGroup>
            <SidebarGroupLabel>Platform</SidebarGroupLabel>
            <SidebarMenu>
                {groups.map((group) => {
                    const Icon = group.icon;
                    return (
                        <Collapsible key={group.title} asChild defaultOpen className="group/collapsible">
                            <SidebarMenuItem>
                                <CollapsibleTrigger asChild>
                                    <SidebarMenuButton tooltip={group.title}>
                                        <Icon />
                                        <span className="text-sm">{group.title}</span>
                                        <ChevronRightIcon className="ml-auto transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90" />
                                    </SidebarMenuButton>
                                </CollapsibleTrigger>
                                <CollapsibleContent>
                                    <SidebarMenuSub>
                                        {group.items.map((link) => (
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
