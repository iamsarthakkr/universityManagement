import { BookOpenIcon, ClipboardListIcon, LibraryIcon } from 'lucide-react';

import { PageHeader } from '@/components/common/PageHeader';
import { QuickLink, QuickLinks } from '@/components/common/QuickLinks';

const STUDENT_LINKS: QuickLink[] = [
    {
        title: 'My courses',
        description: 'Courses you are enrolled in.',
        url: '/dashboard/student/courses',
        icon: LibraryIcon,
    },
    {
        title: 'Enrollments',
        description: 'Your enrollment status, marks, and grades.',
        url: '/dashboard/student/enrollments',
        icon: ClipboardListIcon,
    },
    {
        title: 'Course catalogue',
        description: 'Browse all courses by department.',
        url: '/dashboard/courses',
        icon: BookOpenIcon,
    },
];

export default function StudentHomePage() {
    return (
        <>
            <PageHeader title="Student dashboard" description="Your courses and enrollments in one place." />
            <QuickLinks links={STUDENT_LINKS} />
        </>
    );
}
