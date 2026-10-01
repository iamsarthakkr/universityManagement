import { BookOpenIcon, ShieldCheckIcon, UsersIcon } from 'lucide-react';

import { Role } from '@/types/auth';
import { SidebarNavGroup } from '@/types/navigation';

const SIDEBAR_NAV: SidebarNavGroup[] = [
    {
        title: 'Student Registrations',
        icon: UsersIcon,
        roles: ['ADMIN'],
        items: [
            { title: 'Pending', url: '/dashboard/admin/student-registrations/pending' },
            { title: 'Approved', url: '/dashboard/admin/student-registrations/approved' },
            { title: 'Rejected', url: '/dashboard/admin/student-registrations/rejected' },
        ],
    },
    {
        title: 'Instructor Registrations',
        icon: ShieldCheckIcon,
        roles: ['ADMIN'],
        items: [
            { title: 'Pending', url: '/dashboard/admin/instructor-registrations/pending' },
            { title: 'Approved', url: '/dashboard/admin/instructor-registrations/approved' },
            { title: 'Rejected', url: '/dashboard/admin/instructor-registrations/rejected' },
        ],
    },
    {
        title: 'Courses',
        icon: BookOpenIcon,
        roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'],
        items: [
            { title: 'Catalogue', url: '/dashboard/courses' },
            { title: 'Create Course', url: '/dashboard/courses/new', roles: ['ADMIN'] },
            { title: 'Assigned Courses', url: '/dashboard/instructor/courses', roles: ['INSTRUCTOR'] },
            { title: 'My Courses', url: '/dashboard/student/courses', roles: ['STUDENT'] },
            { title: 'Enrollments', url: '/dashboard/student/enrollments', roles: ['STUDENT'] },
        ],
    },
];

export function getSidebarNav(role: Role): SidebarNavGroup[] {
    return SIDEBAR_NAV.filter((group) => group.roles.includes(role)).map((group) => ({
        ...group,
        items: group.items.filter((item) => !item.roles || item.roles.includes(role)),
    }));
}
