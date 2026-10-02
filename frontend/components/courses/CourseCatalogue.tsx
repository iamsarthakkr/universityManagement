import { useQuery } from '@tanstack/react-query';

import { PageHeader } from '@/components/common/PageHeader';
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

            <section className="rounded-3xl border border-border bg-surface p-6 shadow-soft">
                {catalogueQuery.isPending ? (
                    <p className="text-sm text-text-muted">Loading...</p>
                ) : catalogueQuery.isError ? (
                    <p className="text-sm text-destructive">{catalogueQuery.error.message}</p>
                ) : groups.length === 0 ? (
                    <p className="text-sm text-text-muted">No courses available yet.</p>
                ) : (
                    <div className="divide-y divide-border">
                        {groups.map((group) => (
                            <DepartmentGroup key={group.departmentId} group={group} />
                        ))}
                    </div>
                )}
            </section>
        </>
    );
}
