import { useQuery } from '@tanstack/react-query';
import { PlusIcon } from 'lucide-react';
import { Link } from 'react-router';

import { DepartmentFilter } from '@/components/common/filters/DepartmentFilter';
import { FilterBar } from '@/components/common/filters/FilterBar';
import { InstructorFilter } from '@/components/common/filters/InstructorFilter';
import { SemesterFilter } from '@/components/common/filters/SemesterFilter';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { Button } from '@/components/ui/base/button';
import { idParam, useFilterParams } from '@/hooks/useFilterParams';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { OfferingsTable } from './OfferingsTable';

const FILTERS = {
    semesterId: idParam,
    departmentId: idParam,
    instructorId: idParam,
};

export function OfferingsView() {
    const api = useApi();
    const [filters, setFilters] = useFilterParams(FILTERS);

    const offeringsQuery = useQuery({
        queryKey: queryKeys.offerings.list(filters),
        queryFn: () => unwrap(api.offerings.getOfferings(filters)),
    });
    const offerings = offeringsQuery.data ?? [];
    const isFiltered = Object.keys(filters).length > 0;

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <PageHeader
                    title="Course offerings"
                    description="Courses scheduled in a semester, with their sections and seats."
                />
                <Button asChild>
                    <Link to="/dashboard/admin/offerings/new">
                        <PlusIcon />
                        New offering
                    </Link>
                </Button>
            </div>

            <FilterBar>
                <SemesterFilter value={filters.semesterId} onChange={(semesterId) => setFilters({ semesterId })} />
                <DepartmentFilter
                    value={filters.departmentId}
                    onChange={(departmentId) => setFilters({ departmentId })}
                />
                <InstructorFilter
                    value={filters.instructorId}
                    onChange={(instructorId) => setFilters({ instructorId })}
                />
            </FilterBar>

            <SectionCard className="overflow-hidden p-0">
                <QueryState
                    query={offeringsQuery}
                    isEmpty={offerings.length === 0}
                    emptyMessage={isFiltered ? 'No offerings match these filters.' : 'No offerings yet.'}
                >
                    <OfferingsTable offerings={offerings} />
                </QueryState>
            </SectionCard>
        </div>
    );
}
