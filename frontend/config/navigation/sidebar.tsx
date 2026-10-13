import {
    BookOpenIcon,
    ClipboardListIcon,
    GraduationCapIcon,
    LayoutDashboardIcon,
    LibraryIcon,
    PlusCircleIcon,
    ShieldCheckIcon,
    UsersIcon,
} from 'lucide-react';

import { DASHBOARD_HOME } from '@/config/navigation/dashboardHome';
import { Role } from '@/types/auth';
import { SidebarNavEntry } from '@/types/navigation';

const SIDEBAR_NAV: SidebarNavEntry[] = [
    { title: 'Dashboard', icon: LayoutDashboardIcon, url: DASHBOARD_HOME.ADMIN, roles: ['ADMIN'] },
    { title: 'Dashboard', icon: LayoutDashboardIcon, url: DASHBOARD_HOME.INSTRUCTOR, roles: ['INSTRUCTOR'] },
    { title: 'Dashboard', icon: LayoutDashboardIcon, url: DASHBOARD_HOME.STUDENT, roles: ['STUDENT'] },
    {
        title: 'Registrations',
        icon: ShieldCheckIcon,
        roles: ['ADMIN'],
        items: [
            { title: 'Students', url: '/dashboard/admin/student-registrations' },
            { title: 'Instructors', url: '/dashboard/admin/instructor-registrations' },
        ],
    },
    {
        title: 'Academic',
        icon: GraduationCapIcon,
        roles: ['ADMIN'],
        items: [
            { title: 'Semesters', url: '/dashboard/admin/semesters' },
            { title: 'Course Offerings', url: '/dashboard/admin/offerings' },
            { title: 'Course Catalogue', url: '/dashboard/courses' },
        ],
    },
    {
        title: 'People',
        icon: UsersIcon,
        roles: ['ADMIN'],
        items: [
            { title: 'Students', url: '/dashboard/admin/students' },
            { title: 'Instructors', url: '/dashboard/admin/instructors' },
        ],
    },
    { title: 'My Offerings', icon: LibraryIcon, url: '/dashboard/instructor/courses', roles: ['INSTRUCTOR'] },
    { title: 'Enroll in Courses', icon: PlusCircleIcon, url: '/dashboard/student/offerings', roles: ['STUDENT'] },
    { title: 'My Courses', icon: LibraryIcon, url: '/dashboard/student/courses', roles: ['STUDENT'] },
    { title: 'Enrollments', icon: ClipboardListIcon, url: '/dashboard/student/enrollments', roles: ['STUDENT'] },
    { title: 'Course Catalogue', icon: BookOpenIcon, url: '/dashboard/courses', roles: ['INSTRUCTOR', 'STUDENT'] },
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
