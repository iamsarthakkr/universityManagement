import { PageHeader } from '@/components/common/PageHeader';
import { RegistrationStatus, StudentRegistrationResponse } from '@/types/registration';
import React from 'react';
import { useApi } from '@/stores/apiStore';
import { StudentRegistrationsTable } from './StudentRegistrationsTable';
import { toast } from 'sonner';

type RegistrationViewProps = {
    title: string;
    description: string;
    placeholder?: string;
    status: RegistrationStatus;
};

export const StudentRegistrationsView = ({
    title,
    description,
    placeholder = 'No registration requests found.',
    status,
}: RegistrationViewProps) => {
    const api = useApi();
    const [items, setItems] = React.useState<StudentRegistrationResponse[]>([]);
    const [isLoading, setIsLoading] = React.useState(true);
    const [error, setError] = React.useState<string | null>(null);
    const [pendingId, setPendingId] = React.useState<number | null>(null);

    const loadItems = React.useCallback(async () => {
        const res = await api.admin.getStudentRegistrations(status);
        if (!res.isSuccess) {
            setError(res.message || 'Failed to load registration requests.');
            return;
        }
        setError(null);
        setItems(res.body ?? []);
    }, [api, status]);

    const runAction = React.useCallback(
        async (id: number, action: 'approve' | 'reject') => {
            setPendingId(id);
            try {
                const res =
                    action === 'approve'
                        ? await api.admin.approveStudentRegistration(id)
                        : await api.admin.rejectStudentRegistration(id);

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
        [api, loadItems],
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
                        <p className="text-sm text-muted-foreground">{placeholder}</p>
                    </div>
                ) : (
                    <StudentRegistrationsTable
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
