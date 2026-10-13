import { useQuery } from '@tanstack/react-query';

import { FilterBar } from '@/components/common/filters/FilterBar';
import { FilterSelect } from '@/components/common/filters/FilterSelect';
import { SemesterFilter } from '@/components/common/filters/SemesterFilter';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { useEnrollmentAction } from '@/components/enrollments/useEnrollmentAction';
import { enumParam, idParam, useFilterParams } from '@/hooks/useFilterParams';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { EnrollmentStatus } from '@/types/enrollment';
import { EnrollmentsTable } from './EnrollmentsTable';

const FILTERS = {
    semesterId: idParam,
    status: enumParam(Object.values(EnrollmentStatus)),
};

const STATUS_OPTIONS = [
    { value: EnrollmentStatus.PENDING, label: 'Pending' },
    { value: EnrollmentStatus.ENROLLED, label: 'Enrolled' },
    { value: EnrollmentStatus.REJECTED, label: 'Rejected' },
    { value: EnrollmentStatus.CANCELLED, label: 'Cancelled' },
    { value: EnrollmentStatus.DROPPED, label: 'Dropped' },
];

export function MyEnrollmentsView() {
    const api = useApi();
    const [filters, setFilters] = useFilterParams(FILTERS);
    const { onAction, pendingIds } = useEnrollmentAction();

    const enrollmentsQuery = useQuery({
        queryKey: queryKeys.enrollments.mine(filters),
        queryFn: () => unwrap(api.enrollments.getMyEnrollments(filters)),
    });
    const enrollments = enrollmentsQuery.data ?? [];
    const isFiltered = Object.keys(filters).length > 0;

    return (
        <div className="space-y-6">
            <PageHeader
                title="Enrollments"
                description="Every course you have requested and where each request stands."
            />
            <FilterBar>
                <SemesterFilter value={filters.semesterId} onChange={(semesterId) => setFilters({ semesterId })} />
                <FilterSelect
                    label="Status"
                    allLabel="All statuses"
                    value={filters.status}
                    options={STATUS_OPTIONS}
                    onChange={(status) => setFilters({ status })}
                />
            </FilterBar>
            <SectionCard className="overflow-hidden p-0">
                <QueryState
                    query={enrollmentsQuery}
                    isEmpty={enrollments.length === 0}
                    emptyMessage={
                        isFiltered ? 'No enrollments match these filters.' : 'You have not requested any courses yet.'
                    }
                >
                    <EnrollmentsTable enrollments={enrollments} pendingIds={pendingIds} onAction={onAction} />
                </QueryState>
            </SectionCard>
        </div>
    );
}
