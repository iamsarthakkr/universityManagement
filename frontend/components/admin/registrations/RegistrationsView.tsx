import React from 'react';
import { toast } from 'sonner';

import { PageHeader } from '@/components/common/PageHeader';
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

    const [items, setItems] = React.useState<RegistrationResponse[]>([]);
    const [isLoading, setIsLoading] = React.useState(true);
    const [error, setError] = React.useState<string | null>(null);
    const [pendingId, setPendingId] = React.useState<number | null>(null);

    const statusLabel = STATUS_LABEL[status];
    const title = `${statusLabel} ${kind} registrations`;
    const description =
        status === RegistrationStatus.PENDING
            ? `Review pending ${kind} registration requests.`
            : `${statusLabel} ${kind} registration requests.`;
    const emptyMessage = `No ${statusLabel.toLowerCase()} ${kind} registration requests.`;

    const loadItems = React.useCallback(async () => {
        const res = await registrationApi.list(status);
        if (!res.isSuccess) {
            setError(res.message || 'Failed to load registration requests.');
            return;
        }
        setError(null);
        setItems(res.body ?? []);
    }, [registrationApi, status]);

    const runAction = React.useCallback(
        async (id: number, action: 'approve' | 'reject') => {
            setPendingId(id);
            try {
                const res = action === 'approve' ? await registrationApi.approve(id) : await registrationApi.reject(id);

                if (!res.isSuccess) {
                    toast.error(`Failed to ${action} request`, { description: res.message });
                    return;
                }

                toast.success(
                    action === 'approve' ? 'Request approved successfully!' : 'Request rejected successfully!',
                );
                await loadItems();
            } finally {
                setPendingId(null);
            }
        },
        [registrationApi, loadItems],
    );

    const onApprove = React.useCallback((id: number) => runAction(id, 'approve'), [runAction]);
    const onReject = React.useCallback((id: number) => runAction(id, 'reject'), [runAction]);

    React.useEffect(() => {
        setIsLoading(true);
        loadItems().finally(() => setIsLoading(false));
    }, [loadItems]);

    return (
        <div className="space-y-6">
            <PageHeader title={title} description={description} />

            <div className="overflow-hidden rounded-3xl border border-border bg-surface shadow-soft">
                {isLoading ? (
                    <p className="px-1 py-0.5">Loading...</p>
                ) : error ? (
                    <p className="px-1 py-0.5">{error}</p>
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
