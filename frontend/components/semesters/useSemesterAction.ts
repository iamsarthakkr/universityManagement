import { useMutation, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { Semester, SemesterAction, SemesterActionRequest } from '@/types/semester';

const ACTION_SUCCESS: Record<SemesterAction, string> = {
    [SemesterAction.ACTIVATE]: 'Semester activated.',
    [SemesterAction.COMPLETE]: 'Semester marked as completed.',
    [SemesterAction.CANCEL]: 'Semester cancelled.',
};

export function useSemesterAction() {
    const api = useApi();
    const queryClient = useQueryClient();

    const semesterAction = useMutation({
        mutationFn: (request: SemesterActionRequest) => unwrap(api.semesters.performAction(request)),
        onSuccess: (_, { action }) => {
            toast.success(ACTION_SUCCESS[action]);
            // Offerings and enrollments embed the semester's status, so they are stale too.
            return Promise.all([
                queryClient.invalidateQueries({ queryKey: queryKeys.semesters.all }),
                queryClient.invalidateQueries({ queryKey: queryKeys.offerings.all }),
                queryClient.invalidateQueries({ queryKey: queryKeys.enrollments.all }),
            ]);
        },
        onError: (error) => {
            toast.error('Failed to update semester', { description: error.message });
        },
    });

    const { mutate } = semesterAction;
    const onAction = React.useCallback(
        (semester: Semester, action: SemesterAction) => mutate({ semesterId: semester.id, action }),
        [mutate],
    );
    const pendingId = semesterAction.isPending ? (semesterAction.variables?.semesterId ?? null) : null;

    return { onAction, pendingId };
}
