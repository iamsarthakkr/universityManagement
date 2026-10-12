import { useQuery } from '@tanstack/react-query';
import { PlusIcon } from 'lucide-react';
import { Link } from 'react-router';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { Button } from '@/components/ui/base/button';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { newestFirst } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { SemestersTable } from './SemestersTable';
import { useSemesterAction } from './useSemesterAction';

export function SemestersView() {
    const api = useApi();

    const semestersQuery = useQuery({
        queryKey: queryKeys.semesters.list,
        queryFn: () => unwrap(api.semesters.getSemesters()),
        select: newestFirst,
    });
    const semesters = semestersQuery.data ?? [];

    const { onAction, pendingIds } = useSemesterAction();

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <PageHeader title="Semesters" description="Plan semesters and move them through their lifecycle." />
                <Button asChild>
                    <Link to="/dashboard/admin/semesters/new">
                        <PlusIcon />
                        New semester
                    </Link>
                </Button>
            </div>

            <SectionCard className="overflow-hidden p-0">
                <QueryState query={semestersQuery} isEmpty={semesters.length === 0} emptyMessage="No semesters yet.">
                    <SemestersTable semesters={semesters} pendingIds={pendingIds} onAction={onAction} />
                </QueryState>
            </SectionCard>
        </div>
    );
}
