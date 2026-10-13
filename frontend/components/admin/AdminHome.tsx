import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { BookOpenIcon, CalendarRangeIcon, LayersIcon, PlusCircleIcon, ShieldCheckIcon, UsersIcon } from 'lucide-react';

import { PageHeader } from '@/components/common/PageHeader';
import { QuickLink, QuickLinks } from '@/components/common/QuickLinks';
import { StatCard } from '@/components/ui/StatCard';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { RegistrationStatus } from '@/types/registration';
import { Semester } from '@/types/semester';

const ADMIN_LINKS: QuickLink[] = [
    {
        title: 'Registrations',
        description: 'Review student and instructor account requests.',
        url: '/dashboard/admin/student-registrations',
        icon: ShieldCheckIcon,
    },
    {
        title: 'Semesters',
        description: 'Plan semesters and manage their status.',
        url: '/dashboard/admin/semesters',
        icon: CalendarRangeIcon,
    },
    {
        title: 'Course offerings',
        description: 'Schedule courses in a semester with instructors and seats.',
        url: '/dashboard/admin/offerings',
        icon: LayersIcon,
    },
    {
        title: 'People',
        description: 'Students and instructors by department.',
        url: '/dashboard/admin/students',
        icon: UsersIcon,
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

const PENDING = { status: RegistrationStatus.PENDING };

function countOf(query: UseQueryResult<number>) {
    if (query.isSuccess) return String(query.data);
    return query.isError ? '—' : '…';
}

const length = (items: unknown[]) => items.length;
const openForRegistration = (semesters: Semester[]) =>
    semesters.filter((semester) => semester.isRegistrationOpen).length;

export function AdminHome() {
    const api = useApi();

    const pendingStudents = useQuery({
        queryKey: queryKeys.registrations.list('student', PENDING),
        queryFn: () => unwrap(api.registration.getStudentRegistrations(PENDING)),
        select: length,
    });
    const pendingInstructors = useQuery({
        queryKey: queryKeys.registrations.list('instructor', PENDING),
        queryFn: () => unwrap(api.registration.getInstructorRegistrations(PENDING)),
        select: length,
    });
    const openSemesters = useQuery({
        queryKey: queryKeys.semesters.list,
        queryFn: () => unwrap(api.semesters.getSemesters()),
        select: openForRegistration,
    });
    const offerings = useQuery({
        queryKey: queryKeys.offerings.list(),
        queryFn: () => unwrap(api.offerings.getOfferings({})),
        select: length,
    });

    return (
        <div className="space-y-8">
            <PageHeader
                title="Admin dashboard"
                description="Review registration requests, plan semesters, schedule offerings and manage the course catalogue."
            />
            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <StatCard
                    label="Student requests pending"
                    value={countOf(pendingStudents)}
                    to="/dashboard/admin/student-registrations"
                />
                <StatCard
                    label="Instructor requests pending"
                    value={countOf(pendingInstructors)}
                    to="/dashboard/admin/instructor-registrations"
                />
                <StatCard
                    label="Semesters open for registration"
                    value={countOf(openSemesters)}
                    to="/dashboard/admin/semesters"
                />
                <StatCard label="Offerings scheduled" value={countOf(offerings)} to="/dashboard/admin/offerings" />
            </div>
            <QuickLinks links={ADMIN_LINKS} />
        </div>
    );
}
