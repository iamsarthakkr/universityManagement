import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { PageHeader } from '@/components/common/PageHeader';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { RemoteCall } from '@/types/common';
import { IApi } from '@/types/IApi';
import { RegistrationKind, RegistrationResponse, RegistrationStatus } from '@/types/registration';
import { RegistrationsTable } from './RegistrationsTable';

type RegistrationApi = {
    list: RemoteCall<RegistrationStatus, RegistrationResponse[]>;
    approve: RemoteCall<number, unknown>;
    reject: RemoteCall<number, unknown>;
};

function getRegistrationApi(api: IApi, kind: RegistrationKind): RegistrationApi {
    switch (kind) {
        case 'student':
            return {
                list: api.admin.getStudentRegistrations,
                approve: api.admin.approveStudentRegistration,
                reject: api.admin.rejectStudentRegistration,
            };
        case 'instructor':
            return {
                list: api.admin.getInstructorRegistrations,
                approve: api.admin.approveInstructorRegistration,
                reject: api.admin.rejectInstructorRegistration,
            };
    }
}

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
        queryFn: () => unwrap(registrationApi.list(status)),
    });
    const items = registrationsQuery.data ?? [];

    const registrationAction = useMutation({
        mutationFn: ({ id, action }: { id: number; action: 'approve' | 'reject' }) =>
            unwrap(action === 'approve' ? registrationApi.approve(id) : registrationApi.reject(id)),
        onSuccess: (_, { action }) => {
            toast.success(action === 'approve' ? 'Request approved successfully!' : 'Request rejected successfully!');
            return queryClient.invalidateQueries({ queryKey: queryKeys.registrations.byKind(kind) });
        },
        onError: (error, { action }) => {
            toast.error(`Failed to ${action} request`, { description: error.message });
        },
    });

    const pendingId = registrationAction.isPending ? (registrationAction.variables?.id ?? null) : null;

    const { mutate: runAction } = registrationAction;
    const onApprove = React.useCallback((id: number) => runAction({ id, action: 'approve' }), [runAction]);
    const onReject = React.useCallback((id: number) => runAction({ id, action: 'reject' }), [runAction]);

    return (
        <div className="space-y-6">
            <PageHeader title={title} description={description} />

            <div className="overflow-hidden rounded-3xl border border-border bg-surface shadow-soft">
                {registrationsQuery.isPending ? (
                    <p className="px-1 py-0.5">Loading...</p>
                ) : registrationsQuery.isError ? (
                    <p className="px-1 py-0.5">{registrationsQuery.error.message}</p>
                ) : items.length === 0 ? (
                    <div className="p-6">
                        <p className="text-sm text-muted-foreground">{emptyMessage}</p>
                    </div>
                ) : (
                    <RegistrationsTable
                        kind={kind}
                        items={items}
                        pendingId={pendingId}
                        onApprove={onApprove}
                        onReject={onReject}
                    />
                )}
            </div>
        </div>
    );
};
