import { BookOpenIcon, LibraryIcon } from 'lucide-react';

import { PageHeader } from '@/components/common/PageHeader';
import { QuickLink, QuickLinks } from '@/components/common/QuickLinks';

const INSTRUCTOR_LINKS: QuickLink[] = [
    {
        title: 'Assigned courses',
        description: 'Courses you are assigned to teach.',
        url: '/dashboard/instructor/courses',
        icon: LibraryIcon,
    },
    {
        title: 'Course catalogue',
        description: 'Browse all courses by department.',
        url: '/dashboard/courses',
        icon: BookOpenIcon,
    },
];

export default function InstructorHomePage() {
    return (
        <>
            <PageHeader title="Instructor dashboard" description="Your assigned courses and the course catalogue." />
            <QuickLinks links={INSTRUCTOR_LINKS} />
        </>
    );
}
