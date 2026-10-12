import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { RemoteCall } from '@/types/common';
import { IApi } from '@/types/IApi';
import {
    RegistrationAction,
    RegistrationFilter,
    RegistrationKind,
    RegistrationResponse,
    RegistrationStatus,
} from '@/types/registration';
import { RegistrationsTable } from './RegistrationsTable';

type RegistrationApi = {
    list: RemoteCall<RegistrationFilter, RegistrationResponse[]>;
    performAction: RemoteCall<{ registrationId: number; action: RegistrationAction }, RegistrationResponse>;
};

function getRegistrationApi(api: IApi, kind: RegistrationKind): RegistrationApi {
    switch (kind) {
        case 'student':
            return {
                list: api.registration.getStudentRegistrations,
                performAction: api.registration.performStudentRegistrationAction,
            };
        case 'instructor':
            return {
                list: api.registration.getInstructorRegistrations,
                performAction: api.registration.performInstructorRegistrationAction,
            };
    }
}

const ACTION_SUCCESS: Record<RegistrationAction, string> = {
    [RegistrationAction.APPROVE]: 'Request approved successfully!',
    [RegistrationAction.REJECT]: 'Request rejected successfully!',
};

const STATUS_LABEL: Record<RegistrationStatus, string> = {
    [RegistrationStatus.PENDING]: 'Pending',
    [RegistrationStatus.APPROVED]: 'Approved',
    [RegistrationStatus.REJECTED]: 'Rejected',
};

type Props = {
    kind: RegistrationKind;
    status: RegistrationStatus;
};

export const RegistrationsView = ({ kind, status }: Props) => {
    const api = useApi();
    const registrationApi = React.useMemo(() => getRegistrationApi(api, kind), [api, kind]);

    const statusLabel = STATUS_LABEL[status];
    const title = `${statusLabel} ${kind} registrations`;
    const description =
        status === RegistrationStatus.PENDING
            ? `Review pending ${kind} registration requests.`
            : `${statusLabel} ${kind} registration requests.`;
    const emptyMessage = `No ${statusLabel.toLowerCase()} ${kind} registration requests.`;

    const queryClient = useQueryClient();

    const registrationsQuery = useQuery({
        queryKey: queryKeys.registrations.list(kind, status),
        queryFn: () => unwrap(registrationApi.list({ status })),
    });
    const items = registrationsQuery.data ?? [];

    const registrationAction = useMutation({
        mutationFn: ({ id, action }: { id: number; action: RegistrationAction }) =>
            unwrap(registrationApi.performAction({ registrationId: id, action })),
        onSuccess: (_, { action }) => {
            toast.success(ACTION_SUCCESS[action]);
            return queryClient.invalidateQueries({ queryKey: queryKeys.registrations.byKind(kind) });
        },
        onError: (error, { action }) => {
            toast.error(`Failed to ${action.toLowerCase()} request`, { description: error.message });
        },
    });

    const pendingId = registrationAction.isPending ? (registrationAction.variables?.id ?? null) : null;
    const { mutate: runAction } = registrationAction;
    const onAction = React.useCallback(
        (id: number, action: RegistrationAction) => runAction({ id, action }),
        [runAction],
    );

    return (
        <div className="space-y-6">
            <PageHeader title={title} description={description} />

            <SectionCard className="overflow-hidden p-0">
                <QueryState query={registrationsQuery} isEmpty={items.length === 0} emptyMessage={emptyMessage}>
                    <RegistrationsTable kind={kind} items={items} pendingId={pendingId} onAction={onAction} />
                </QueryState>
            </SectionCard>
        </div>
    );
};
