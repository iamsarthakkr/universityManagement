import { useQuery } from '@tanstack/react-query';

import { FilterBar } from '@/components/common/filters/FilterBar';
import { SemesterFilter } from '@/components/common/filters/SemesterFilter';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { OfferingCardGroups } from '@/components/offerings/OfferingCardGroups';
import { idParam, useFilterParams } from '@/hooks/useFilterParams';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';

const FILTERS = { semesterId: idParam };

export function MyOfferingsView() {
    const api = useApi();
    const [filters, setFilters] = useFilterParams(FILTERS);

    const offeringsQuery = useQuery({
        queryKey: queryKeys.offerings.mine(filters),
        queryFn: () => unwrap(api.offerings.getMyOfferings(filters)),
    });
    const offerings = offeringsQuery.data ?? [];

    return (
        <div className="space-y-6">
            <PageHeader title="My offerings" description="Courses you teach. Open one to review its students." />
            <FilterBar>
                <SemesterFilter value={filters.semesterId} onChange={(semesterId) => setFilters({ semesterId })} />
            </FilterBar>
            <QueryState
                query={offeringsQuery}
                isEmpty={offerings.length === 0}
                emptyMessage={
                    filters.semesterId ? 'You teach no offerings in this semester.' : 'You have no offerings yet.'
                }
            >
                <OfferingCardGroups offerings={offerings} />
            </QueryState>
        </div>
    );
}
