import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { PlusIcon } from 'lucide-react';
import React from 'react';
import { Link } from 'react-router';
import { toast } from 'sonner';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '@/components/ui/base/alertDialog';
import { Button } from '@/components/ui/base/button';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { formatSemesterName } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { Semester, SemesterStatus, SemesterTransitionRequest } from '@/types/semester';
import { SemestersTable } from './SemestersTable';

const TRANSITION_SUCCESS: Record<SemesterStatus, string> = {
    [SemesterStatus.PLANNED]: 'Semester moved to planned.',
    [SemesterStatus.ACTIVE]: 'Semester activated.',
    [SemesterStatus.COMPLETED]: 'Semester marked as completed.',
    [SemesterStatus.CANCELLED]: 'Semester cancelled.',
};

function newestFirst(semesters: Semester[]) {
    return [...semesters].sort((a, b) => b.startDate.localeCompare(a.startDate));
}

export function SemestersView() {
    const api = useApi();
    const queryClient = useQueryClient();

    const semestersQuery = useQuery({
        queryKey: queryKeys.semesters.list,
        queryFn: () => unwrap(api.semesters.getSemesters()),
        select: newestFirst,
    });
    const semesters = semestersQuery.data ?? [];

    const transition = useMutation({
        mutationFn: (request: SemesterTransitionRequest) => unwrap(api.semesters.transitionSemester(request)),
        onSuccess: (_, { status }) => {
            toast.success(TRANSITION_SUCCESS[status]);
            return queryClient.invalidateQueries({ queryKey: queryKeys.semesters.all });
        },
        onError: (error) => {
            toast.error('Failed to update semester', { description: error.message });
        },
    });

    // Cancelling can't be undone, so it waits for confirmation; other transitions run immediately.
    const [confirmCancel, setConfirmCancel] = React.useState<Semester | null>(null);

    const { mutate: runTransition } = transition;
    const onTransition = React.useCallback(
        (semester: Semester, status: SemesterStatus) => {
            if (status === SemesterStatus.CANCELLED) {
                setConfirmCancel(semester);
                return;
            }
            runTransition({ semesterId: semester.id, status });
        },
        [runTransition],
    );

    const pendingId = transition.isPending ? (transition.variables?.semesterId ?? null) : null;

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
                    <SemestersTable semesters={semesters} pendingId={pendingId} onTransition={onTransition} />
                </QueryState>
            </SectionCard>

            <AlertDialog open={confirmCancel !== null} onOpenChange={(open) => !open && setConfirmCancel(null)}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>
                            Cancel {confirmCancel ? formatSemesterName(confirmCancel) : 'semester'}?
                        </AlertDialogTitle>
                        <AlertDialogDescription>This can&apos;t be undone.</AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Keep semester</AlertDialogCancel>
                        <AlertDialogAction
                            variant="destructive"
                            onClick={() => {
                                if (confirmCancel) {
                                    runTransition({ semesterId: confirmCancel.id, status: SemesterStatus.CANCELLED });
                                }
                            }}
                        >
                            Cancel semester
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
