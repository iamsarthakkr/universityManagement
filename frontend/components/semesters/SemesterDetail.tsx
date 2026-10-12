import { useQuery } from '@tanstack/react-query';
import { ArrowLeftIcon, PlusIcon } from 'lucide-react';
import { ReactNode } from 'react';
import { Link } from 'react-router';

import { ActionMenu } from '@/components/common/ActionMenu';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { OfferingsTable } from '@/components/offerings/OfferingsTable';
import { Button } from '@/components/ui/base/button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { formatIsoDateRange } from '@/lib/date';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { formatSemesterName } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { Semester } from '@/types/semester';
import { SEMESTER_ACTIONS } from './semesterActions';
import { SemesterStatusBadge } from './SemesterStatusBadge';
import { useSemesterAction } from './useSemesterAction';

function Detail({ label, children }: { label: string; children: ReactNode }) {
    return (
        <div className="flex flex-col gap-1">
            <dt className="text-xs font-medium uppercase tracking-wide text-text-muted">{label}</dt>
            <dd className="text-sm text-text">{children}</dd>
        </div>
    );
}

function SemesterSummaryCard({ semester }: { semester: Semester }) {
    return (
        <SectionCard>
            <dl className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
                <Detail label="Status">
                    <SemesterStatusBadge status={semester.status} />
                </Detail>
                <Detail label="Registration window">
                    <div className="flex flex-col items-start gap-1">
                        {formatIsoDateRange(semester.registrationStartDate, semester.registrationEndDate)}
                        <StatusBadge tone={semester.isRegistrationOpen ? 'success' : 'neutral'}>
                            {semester.isRegistrationOpen ? 'Open now' : 'Closed'}
                        </StatusBadge>
                    </div>
                </Detail>
                <Detail label="Teaching period">{formatIsoDateRange(semester.startDate, semester.endDate)}</Detail>
                <Detail label="New offerings">
                    <StatusBadge tone={semester.acceptsOfferings ? 'info' : 'neutral'}>
                        {semester.acceptsOfferings ? 'Accepted' : 'Not accepted'}
                    </StatusBadge>
                </Detail>
            </dl>
        </SectionCard>
    );
}

function SemesterOfferings({ semester }: { semester: Semester }) {
    const api = useApi();
    const filter = { semesterId: semester.id };
    const offeringsQuery = useQuery({
        queryKey: queryKeys.offerings.list(filter),
        queryFn: () => unwrap(api.offerings.getOfferings(filter)),
    });
    const offerings = offeringsQuery.data ?? [];

    return (
        <SectionCard className="overflow-hidden p-0">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border px-6 py-4">
                <h2 className="text-lg font-semibold text-text">Course offerings</h2>
                {semester.acceptsOfferings && (
                    <Button asChild size="sm">
                        <Link to={`/dashboard/admin/offerings/new?semesterId=${semester.id}`}>
                            <PlusIcon />
                            Add offering
                        </Link>
                    </Button>
                )}
            </div>
            <QueryState
                query={offeringsQuery}
                isEmpty={offerings.length === 0}
                emptyMessage="No offerings in this semester yet."
            >
                <OfferingsTable offerings={offerings} showSemester={false} />
            </QueryState>
        </SectionCard>
    );
}

export function SemesterDetail({ semesterId }: { semesterId: number }) {
    const api = useApi();
    const semesterQuery = useQuery({
        queryKey: queryKeys.semesters.detail(semesterId),
        queryFn: () => unwrap(api.semesters.getSemester(semesterId)),
    });
    const { onAction, pendingId } = useSemesterAction();
    const semester = semesterQuery.data;
    const name = semester ? formatSemesterName(semester) : 'Semester';

    return (
        <div className="space-y-6">
            <Button asChild variant="ghost" size="sm" className="-ml-2 text-text-muted">
                <Link to="/dashboard/admin/semesters">
                    <ArrowLeftIcon />
                    All semesters
                </Link>
            </Button>

            <div className="flex items-start justify-between gap-4">
                <PageHeader title={name} description="Semester details and the courses offered in it." />
                {semester && (
                    <ActionMenu
                        actions={semester.allowedActions}
                        config={SEMESTER_ACTIONS}
                        subject={name}
                        disabled={pendingId === semester.id}
                        onAction={(action) => onAction(semester, action)}
                    />
                )}
            </div>

            {semester ? (
                <>
                    <SemesterSummaryCard semester={semester} />
                    <SemesterOfferings semester={semester} />
                </>
            ) : (
                <SectionCard>
                    <QueryState query={semesterQuery} isEmpty={false} emptyMessage="">
                        {null}
                    </QueryState>
                </SectionCard>
            )}
        </div>
    );
}
