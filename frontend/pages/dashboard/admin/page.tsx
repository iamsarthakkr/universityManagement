import { BookOpenIcon, CalendarRangeIcon, PlusCircleIcon, ShieldCheckIcon, UsersIcon } from 'lucide-react';

import { PageHeader } from '@/components/common/PageHeader';
import { QuickLink, QuickLinks } from '@/components/common/QuickLinks';

const ADMIN_LINKS: QuickLink[] = [
    {
        title: 'Student registrations',
        description: 'Review and approve pending student requests.',
        url: '/dashboard/admin/student-registrations/pending',
        icon: UsersIcon,
    },
    {
        title: 'Instructor registrations',
        description: 'Review and approve pending instructor requests.',
        url: '/dashboard/admin/instructor-registrations/pending',
        icon: ShieldCheckIcon,
    },
    {
        title: 'Semesters',
        description: 'Plan semesters and manage their status.',
        url: '/dashboard/admin/semesters',
        icon: CalendarRangeIcon,
    },
    {
        title: 'Create course',
        description: 'Add a new course to the catalogue.',
        url: '/dashboard/courses/new',
        icon: PlusCircleIcon,
    },
    {
        title: 'Course catalogue',
        description: 'Browse all courses by department.',
        url: '/dashboard/courses',
        icon: BookOpenIcon,
    },
];

export default function AdminDashboardPage() {
    return (
        <>
            <PageHeader
                title="Admin dashboard"
                description="Review registration requests, plan semesters and manage the course catalogue."
            />
            <QuickLinks links={ADMIN_LINKS} />
        </>
    );
}
