import { useMutation, useMutationState, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { EnrollmentAction, EnrollmentActionRequest } from '@/types/enrollment';

const MUTATION_KEY = ['enrollments', 'action'];

const ACTION_SUCCESS: Record<EnrollmentAction, string> = {
    [EnrollmentAction.APPROVE]: 'Enrollment approved.',
    [EnrollmentAction.REJECT]: 'Request rejected.',
    [EnrollmentAction.CANCEL]: 'Request cancelled.',
    [EnrollmentAction.DROP]: 'Course dropped.',
};

export function useEnrollmentAction() {
    const api = useApi();
    const queryClient = useQueryClient();

    const enrollmentAction = useMutation({
        mutationKey: MUTATION_KEY,
        mutationFn: (request: EnrollmentActionRequest) => unwrap(api.enrollments.performAction(request)),
        onSuccess: (_, { action }) => {
            toast.success(ACTION_SUCCESS[action]);
            // Approving or dropping changes seat counts, which every offering view shows.
            return Promise.all([
                queryClient.invalidateQueries({ queryKey: queryKeys.offerings.all }),
                queryClient.invalidateQueries({ queryKey: queryKeys.enrollments.all }),
            ]);
        },
        onError: (error) => {
            toast.error('Failed to update enrollment', { description: error.message });
        },
    });

    const { mutate } = enrollmentAction;
    const onAction = React.useCallback(
        (enrollmentId: number, action: EnrollmentAction) => mutate({ enrollmentId, action }),
        [mutate],
    );
    // Every in-flight row stays disabled, not only the latest one.
    const pendingIds = useMutationState({
        filters: { mutationKey: MUTATION_KEY, status: 'pending' },
        select: (mutation) => (mutation.state.variables as EnrollmentActionRequest).enrollmentId,
    });

    return { onAction, pendingIds };
}
