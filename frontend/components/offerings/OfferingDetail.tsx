import { useQuery } from '@tanstack/react-query';
import { ArrowLeftIcon } from 'lucide-react';
import { Link } from 'react-router';

import { DetailItem } from '@/components/common/DetailItem';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { StatusTab, StatusTabs } from '@/components/common/StatusTabs';
import { RosterTable } from '@/components/enrollments/RosterTable';
import { useEnrollmentAction } from '@/components/enrollments/useEnrollmentAction';
import { SemesterStatusBadge } from '@/components/semesters/SemesterStatusBadge';
import { Button } from '@/components/ui/base/button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { enumParam, useFilterParams } from '@/hooks/useFilterParams';
import { formatCredits, fullName } from '@/lib/format';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { formatSemesterName } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { useAppStore } from '@/stores/appStore';
import { EnrollmentStatus } from '@/types/enrollment';
import { CourseOffering } from '@/types/offering';
import { OfferingSeats } from './OfferingSeats';

const FILTERS = { status: enumParam(Object.values(EnrollmentStatus)) };

const TABS: StatusTab<EnrollmentStatus>[] = [
    { value: undefined, label: 'All' },
    { value: EnrollmentStatus.PENDING, label: 'Pending' },
    { value: EnrollmentStatus.ENROLLED, label: 'Enrolled' },
];

const EMPTY_MESSAGE: Record<EnrollmentStatus | 'ALL', string> = {
    PENDING: 'No pending requests.',
    ENROLLED: 'No students enrolled yet.',
    REJECTED: 'No rejected requests.',
    CANCELLED: 'No cancelled requests.',
    DROPPED: 'No dropped enrollments.',
    ALL: 'No enrollment requests yet.',
};

function OfferingSummaryCard({ offering }: { offering: CourseOffering }) {
    return (
        <SectionCard>
            <dl className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
                <DetailItem label="Semester">
                    <div className="flex flex-col items-start gap-1">
                        {formatSemesterName(offering.semester)}
                        <SemesterStatusBadge status={offering.semester.status} />
                    </div>
                </DetailItem>
                <DetailItem label="Registration">
                    <StatusBadge tone={offering.semester.isRegistrationOpen ? 'success' : 'neutral'}>
                        {offering.semester.isRegistrationOpen ? 'Open now' : 'Closed'}
                    </StatusBadge>
                </DetailItem>
                <DetailItem label="Instructor">{fullName(offering.instructor)}</DetailItem>
                <DetailItem label="Seats">
                    <OfferingSeats
                        enrolled={offering.enrolled}
                        capacity={offering.capacity}
                        seatsAvailable={offering.seatsAvailable}
                    />
                </DetailItem>
            </dl>
        </SectionCard>
    );
}

function Roster({ offeringId }: { offeringId: number }) {
    const api = useApi();
    const [filters, setFilters] = useFilterParams(FILTERS);
    const { onAction, pendingIds } = useEnrollmentAction();

    const rosterQuery = useQuery({
        queryKey: queryKeys.offerings.enrollments(offeringId, filters.status),
        queryFn: () => unwrap(api.offerings.getEnrollments({ offeringId, status: filters.status })),
    });
    const enrollments = rosterQuery.data ?? [];

    return (
        <SectionCard className="overflow-hidden p-0">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border px-6 py-4">
                <h2 className="text-lg font-semibold text-text">Students</h2>
                <StatusTabs
                    label="Enrollment status"
                    tabs={TABS}
                    value={filters.status}
                    onChange={(status) => setFilters({ status })}
                />
            </div>
            <QueryState
                query={rosterQuery}
                isEmpty={enrollments.length === 0}
                emptyMessage={EMPTY_MESSAGE[filters.status ?? 'ALL']}
            >
                <RosterTable enrollments={enrollments} pendingIds={pendingIds} onAction={onAction} />
            </QueryState>
        </SectionCard>
    );
}

function BackLink({ offering }: { offering: CourseOffering | undefined }) {
    const role = useAppStore((state) => state.user?.role);
    const [to, label] =
        role === 'ADMIN'
            ? offering
                ? [`/dashboard/admin/semesters/${offering.semester.id}`, formatSemesterName(offering.semester)]
                : ['/dashboard/admin/offerings', 'All offerings']
            : ['/dashboard/instructor/courses', 'My offerings'];

    return (
        <Button asChild variant="ghost" size="sm" className="-ml-2 text-text-muted">
            <Link to={to}>
                <ArrowLeftIcon />
                {label}
            </Link>
        </Button>
    );
}

export function OfferingDetail({ offeringId }: { offeringId: number }) {
    const api = useApi();
    const offeringQuery = useQuery({
        queryKey: queryKeys.offerings.detail(offeringId),
        queryFn: () => unwrap(api.offerings.getOffering(offeringId)),
    });
    const offering = offeringQuery.data;

    return (
        <div className="space-y-6">
            <BackLink offering={offering} />
            <PageHeader
                title={offering ? `${offering.course.code} · ${offering.course.title}` : 'Course offering'}
                description={
                    offering
                        ? `Section ${offering.section} · ${formatCredits(offering.course.credits)}`
                        : 'Offering details and its students.'
                }
            />
            {offering ? (
                <>
                    <OfferingSummaryCard offering={offering} />
                    <Roster offeringId={offering.id} />
                </>
            ) : (
                <SectionCard>
                    <QueryState query={offeringQuery} isEmpty={false} emptyMessage="">
                        {null}
                    </QueryState>
                </SectionCard>
            )}
        </div>
    );
}
