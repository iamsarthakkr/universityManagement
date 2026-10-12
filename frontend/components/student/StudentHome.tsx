import { useQuery } from '@tanstack/react-query';
import { BookOpenIcon, ClipboardListIcon, LibraryIcon, PlusCircleIcon } from 'lucide-react';
import { Link } from 'react-router';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { QuickLink, QuickLinks } from '@/components/common/QuickLinks';
import { Button } from '@/components/ui/base/button';
import { StatCard } from '@/components/ui/StatCard';
import { formatIsoDate } from '@/lib/date';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { formatSemesterName } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { EnrollmentStatus } from '@/types/enrollment';
import { Semester } from '@/types/semester';

const STUDENT_LINKS: QuickLink[] = [
    {
        title: 'Enroll in courses',
        description: 'Browse offerings and request a seat.',
        url: '/dashboard/student/offerings',
        icon: PlusCircleIcon,
    },
    {
        title: 'My courses',
        description: 'Courses you are enrolled in.',
        url: '/dashboard/student/courses',
        icon: LibraryIcon,
    },
    {
        title: 'Enrollments',
        description: 'Every request and where it stands.',
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

function openForRegistration(semesters: Semester[]) {
    return semesters.filter((semester) => semester.isRegistrationOpen);
}

function RegistrationBanner({ semesters }: { semesters: Semester[] }) {
    return (
        <section
            aria-label="Registration open"
            className="flex flex-col gap-4 rounded-3xl border border-brand/30 bg-brand-soft/60 p-6 sm:flex-row sm:items-center sm:justify-between"
        >
            <div className="flex flex-col gap-1">
                {semesters.map((semester) => (
                    <p key={semester.id} className="text-sm text-text">
                        Registration for <span className="font-semibold">{formatSemesterName(semester)}</span> is open
                        until {formatIsoDate(semester.registrationEndDate)}.
                    </p>
                ))}
            </div>
            <Button asChild>
                <Link to="/dashboard/student/offerings">Browse offerings</Link>
            </Button>
        </section>
    );
}

export function StudentHome() {
    const api = useApi();
    const semestersQuery = useQuery({
        queryKey: queryKeys.semesters.list,
        queryFn: () => unwrap(api.semesters.getSemesters()),
        select: openForRegistration,
    });
    const enrollmentsQuery = useQuery({
        queryKey: queryKeys.enrollments.mine(),
        queryFn: () => unwrap(api.enrollments.getMyEnrollments({})),
    });

    const enrollments = enrollmentsQuery.data ?? [];
    const count = (status: EnrollmentStatus) => enrollments.filter((enrollment) => enrollment.status === status).length;
    const openSemesters = semestersQuery.data ?? [];

    return (
        <div className="space-y-8">
            <PageHeader title="Student dashboard" description="Your courses and enrollments in one place." />

            {openSemesters.length > 0 && <RegistrationBanner semesters={openSemesters} />}

            <QueryState query={enrollmentsQuery} isEmpty={false} emptyMessage="">
                <div className="grid gap-4 sm:grid-cols-2">
                    <StatCard label="Courses enrolled" value={String(count(EnrollmentStatus.ENROLLED))} />
                    <StatCard label="Requests awaiting approval" value={String(count(EnrollmentStatus.PENDING))} />
                </div>
            </QueryState>

            <QuickLinks links={STUDENT_LINKS} />
        </div>
    );
}
