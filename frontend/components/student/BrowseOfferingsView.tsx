import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

import { DepartmentFilter } from '@/components/common/filters/DepartmentFilter';
import { FilterBar } from '@/components/common/filters/FilterBar';
import { SemesterFilter } from '@/components/common/filters/SemesterFilter';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { StatusTab, StatusTabs } from '@/components/common/StatusTabs';
import { SemesterSections } from '@/components/semesters/SemesterSections';
import { enumParam, idParam, useFilterParams } from '@/hooks/useFilterParams';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { groupBySemester } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { EnrollmentStatus } from '@/types/enrollment';
import { CourseOffering } from '@/types/offering';
import { StudentOfferingCard } from './StudentOfferingCard';

type View = 'ALL';

const FILTERS = {
    semesterId: idParam,
    departmentId: idParam,
    view: enumParam<View>(['ALL']),
};

const VIEW_TABS: StatusTab<View>[] = [
    { value: undefined, label: 'Open for registration' },
    { value: 'ALL', label: 'All semesters' },
];

export function BrowseOfferingsView() {
    const api = useApi();
    const queryClient = useQueryClient();
    const [filters, setFilters] = useFilterParams(FILTERS);
    const { view, ...serverFilter } = filters;

    const offeringsQuery = useQuery({
        queryKey: queryKeys.offerings.list(serverFilter),
        queryFn: () => unwrap(api.offerings.getOfferings(serverFilter)),
    });
    const enrollmentsQuery = useQuery({
        queryKey: queryKeys.enrollments.mine(),
        queryFn: () => unwrap(api.enrollments.getMyEnrollments({})),
    });

    const myStatusByOffering = new Map<number, EnrollmentStatus>(
        (enrollmentsQuery.data ?? []).map((enrollment) => [enrollment.courseOffering.id, enrollment.status]),
    );
    const offerings = (offeringsQuery.data ?? []).filter(
        (offering) => view === 'ALL' || offering.semester.isRegistrationOpen,
    );
    // The server lists oldest semesters first; upcoming ones matter most when choosing courses.
    const groups = groupBySemester(offerings, (offering) => offering.semester).reverse();

    const requestEnrollment = useMutation({
        mutationFn: (offering: CourseOffering) => unwrap(api.offerings.requestEnrollment(offering.id)),
        onSuccess: (_, offering) => {
            toast.success(`Requested ${offering.course.code}. You'll see it under Enrollments.`);
            return Promise.all([
                queryClient.invalidateQueries({ queryKey: queryKeys.offerings.all }),
                queryClient.invalidateQueries({ queryKey: queryKeys.enrollments.all }),
            ]);
        },
        onError: (error) => {
            toast.error('Request failed', { description: error.message });
        },
    });
    const requestingId = requestEnrollment.isPending ? requestEnrollment.variables?.id : undefined;

    const emptyMessage =
        view === 'ALL' ? 'No offerings match these filters.' : 'No offerings are open for registration right now.';

    return (
        <div className="space-y-6">
            <PageHeader
                title="Enroll in courses"
                description="Request a seat in an offering. The instructor or an admin approves it."
            />
            <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
                <FilterBar>
                    <SemesterFilter value={filters.semesterId} onChange={(semesterId) => setFilters({ semesterId })} />
                    <DepartmentFilter
                        value={filters.departmentId}
                        onChange={(departmentId) => setFilters({ departmentId })}
                    />
                </FilterBar>
                <StatusTabs
                    label="Which semesters"
                    tabs={VIEW_TABS}
                    value={view}
                    onChange={(next) => setFilters({ view: next })}
                />
            </div>
            <QueryState query={offeringsQuery} isEmpty={offerings.length === 0} emptyMessage={emptyMessage}>
                <SemesterSections
                    groups={groups}
                    itemKey={(offering) => offering.id}
                    renderItem={(offering) => (
                        <StudentOfferingCard
                            offering={offering}
                            myStatus={myStatusByOffering.get(offering.id)}
                            isRequesting={requestingId === offering.id}
                            disabled={requestEnrollment.isPending}
                            onRequest={requestEnrollment.mutate}
                        />
                    )}
                />
            </QueryState>
        </div>
    );
}
