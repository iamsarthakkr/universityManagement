import { BookOpenIcon, CalendarRangeIcon, LayoutDashboardIcon, ShieldCheckIcon, UsersIcon } from 'lucide-react';

import { DASHBOARD_HOME } from '@/config/navigation/dashboardHome';
import { Role } from '@/types/auth';
import { SidebarNavEntry } from '@/types/navigation';

const SIDEBAR_NAV: SidebarNavEntry[] = [
    { title: 'Dashboard', icon: LayoutDashboardIcon, url: DASHBOARD_HOME.ADMIN, roles: ['ADMIN'] },
    { title: 'Dashboard', icon: LayoutDashboardIcon, url: DASHBOARD_HOME.INSTRUCTOR, roles: ['INSTRUCTOR'] },
    { title: 'Dashboard', icon: LayoutDashboardIcon, url: DASHBOARD_HOME.STUDENT, roles: ['STUDENT'] },
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
        title: 'Semesters',
        icon: CalendarRangeIcon,
        roles: ['ADMIN'],
        items: [
            { title: 'All Semesters', url: '/dashboard/admin/semesters' },
            { title: 'Create Semester', url: '/dashboard/admin/semesters/new' },
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

export function getSidebarNav(role: Role): SidebarNavEntry[] {
    return SIDEBAR_NAV.filter((entry) => entry.roles.includes(role)).map((entry) => {
        if (!('items' in entry)) {
            return entry;
        }

        return {
            ...entry,
            items: entry.items.filter((item) => !item.roles || item.roles.includes(role)),
        };
    });
}
