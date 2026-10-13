import { useQuery } from '@tanstack/react-query';
import { BookOpenIcon, LibraryIcon } from 'lucide-react';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { QuickLink, QuickLinks } from '@/components/common/QuickLinks';
import { OfferingCard } from '@/components/offerings/OfferingCard';
import { StatCard } from '@/components/ui/StatCard';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';

const INSTRUCTOR_LINKS: QuickLink[] = [
    {
        title: 'My offerings',
        description: 'Courses you teach and their students.',
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

export function InstructorHome() {
    const api = useApi();
    const offeringsQuery = useQuery({
        queryKey: queryKeys.offerings.mine(),
        queryFn: () => unwrap(api.offerings.getMyOfferings({})),
    });
    const offerings = offeringsQuery.data ?? [];
    const takingRequests = offerings.filter((offering) => offering.semester.isRegistrationOpen);
    const enrolled = offerings.reduce((total, offering) => total + offering.enrolled, 0);

    return (
        <div className="space-y-8">
            <PageHeader title="Instructor dashboard" description="Your offerings and the students in them." />

            <QueryState query={offeringsQuery} isEmpty={false} emptyMessage="">
                <div className="grid gap-4 sm:grid-cols-3">
                    <StatCard label="Offerings you teach" value={String(offerings.length)} />
                    <StatCard label="Students enrolled" value={String(enrolled)} />
                    <StatCard label="Open for registration" value={String(takingRequests.length)} />
                </div>
            </QueryState>

            {takingRequests.length > 0 && (
                <section aria-label="Open for registration" className="flex flex-col gap-3">
                    <div>
                        <h2 className="text-lg font-semibold text-text">Open for registration</h2>
                        <p className="text-sm text-text-muted">
                            Students can request these now; check for pending requests.
                        </p>
                    </div>
                    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                        {takingRequests.map((offering) => (
                            <OfferingCard key={offering.id} offering={offering} />
                        ))}
                    </div>
                </section>
            )}

            <QuickLinks links={INSTRUCTOR_LINKS} />
        </div>
    );
}
