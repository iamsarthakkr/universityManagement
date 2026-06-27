import { BookOpenIcon, GraduationCapIcon, PlusCircleIcon, Settings2Icon, ShieldCheckIcon, UsersIcon } from 'lucide-react';

import { SidebarNavItem } from '@/types/navigation';

export const NAV_MAIN: SidebarNavItem[] = [
    {
        title: 'Student Registrations',
        url: '/dashboard/admin/student-registrations',
        icon: UsersIcon,
        open: true,

        roles: ['ADMIN'],

        items: [
            {
                title: 'Pending',
                url: '/dashboard/admin/student-registrations/pending',
            },
            {
                title: 'Approved',
                url: '/dashboard/admin/student-registrations/approved',
            },
            {
                title: 'Rejected',
                url: '/dashboard/admin/student-registrations/rejected',
            },
        ],
    },

    {
        title: 'Instructor Registrations',
        url: '/dashboard/admin/instructor-registrations',
        icon: ShieldCheckIcon,
        open: true,

        roles: ['ADMIN'],

        items: [
            {
                title: 'Pending',
                url: '/dashboard/admin/instructor-registrations/pending',
            },
            {
                title: 'Approved',
                url: '/dashboard/admin/instructor-registrations/approved',
            },
            {
                title: 'Rejected',
                url: '/dashboard/admin/instructor-registrations/rejected',
            },
        ],
    },

    {
        title: 'Courses',
        url: '/dashboard/student/courses',
        icon: BookOpenIcon,
        open: true,

        roles: ['STUDENT'],

        items: [
            {
                title: 'My Courses',
                url: '/dashboard/student/courses',
            },
            {
                title: 'Enrollments',
                url: '/dashboard/student/enrollments',
            },
        ],
    },

    {
        title: 'Instructor',
        url: '/dashboard/instructor',
        icon: GraduationCapIcon,
        open: true,

        roles: ['INSTRUCTOR'],

        items: [
            {
                title: 'Assigned Courses',
                url: '/dashboard/instructor/courses',
            },
            {
                title: 'Students',
                url: '/dashboard/instructor/students',
            },
        ],
    },

    {
        title: 'Courses',
        url: '/dashboard/courses',
        icon: PlusCircleIcon,
        open: true,

        roles: ['ADMIN', 'INSTRUCTOR'],

        items: [
            {
                title: 'Create Course',
                url: '/dashboard/courses/new',
            },
        ],
    },

    {
        title: 'Settings',
        url: '/dashboard/settings',
        icon: Settings2Icon,
        open: true,

        roles: ['ADMIN', 'STUDENT', 'INSTRUCTOR'],

        items: [],
    },
];
