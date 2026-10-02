import { useQuery } from '@tanstack/react-query';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';

import { DepartmentGroup } from './DepartmentGroup';

export function CourseCatalogue() {
    const api = useApi();

    const catalogueQuery = useQuery({
        queryKey: queryKeys.courses.catalogue,
        queryFn: () => unwrap(api.courses.getCatalogue()),
    });
    const groups = catalogueQuery.data ?? [];

    return (
        <>
            <PageHeader title="Course catalogue" description="Browse all available courses grouped by department." />

            <SectionCard>
                <QueryState
                    query={catalogueQuery}
                    isEmpty={groups.length === 0}
                    emptyMessage="No courses available yet."
                >
                    <div className="divide-y divide-border">
                        {groups.map((group) => (
                            <DepartmentGroup key={group.departmentId} group={group} />
                        ))}
                    </div>
                </QueryState>
            </SectionCard>
        </>
    );
}
