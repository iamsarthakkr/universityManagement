'use client';

import { PageHeader } from '@/components/common/PageHeader';
import { InstructorRegistrationResponse, RegistrationStatus } from '@/types/registration';
import React from 'react';
import { useApi } from '@/context/ApiContext';
import { InstructorRegistrationsTable } from './InstructorRegistrationsTable';
import { toast } from 'sonner';

type RegistrationViewProps = {
    title: string;
    description: string;
    placeholder?: string;
    status: RegistrationStatus;
};

export const InstructorRegistrationsView = ({
    title,
    description,
    placeholder = 'No registration requests found.',
    status,
}: RegistrationViewProps) => {
    const api = useApi();
    const [items, setItems] = React.useState<InstructorRegistrationResponse[]>([]);
    const [isLoading, setIsLoading] = React.useState(true);
    const [error, setError] = React.useState<string | null>(null);

    const fetchData = React.useCallback(async () => {
        setIsLoading(true);

        const res = await api.admin.getInstructorRegistrations(status);
        if (!res.isSuccess || !res.body) {
            setError(res.message);
            return;
        }
        setItems(res.body);

        setIsLoading(false);
    }, []);

    const onApprove = React.useCallback(
        async (item: InstructorRegistrationResponse) => {
            const res = await api.admin.approveInstructorRegistration(item.id);
            if (res.isSuccess && res.body) {
                await fetchData();
                toast.success('Request approved successfully!');
            } else {
                toast.error('Failed to approve request');
            }
        },
        [api],
    );

    const onReject = React.useCallback(async (item: InstructorRegistrationResponse) => {
        const res = await api.admin.rejectInstructorRegistration(item.id);
        if (res.isSuccess && res.body) {
            await fetchData();
            toast.success('Request rejected request successfully!');
        } else {
            console.warn(res);
            toast.error('Failed to reject request');
        }
    }, []);

    React.useEffect(() => {
        fetchData();
    }, []);

    return (
        <div className="space-y-6">
            <PageHeader title={title} description={description} />

            <div className="overflow-hidden rounded-3xl border border-border bg-white shadow-soft">
                {isLoading ? (
                    <p className="px-1 py-0.5">Loading...</p>
                ) : error ? (
                    <p className="px-1 py-0.5">{error}</p>
                ) : items.length === 0 ? (
                    <div className="p-6">
                        <p className="text-sm text-muted-foreground">{placeholder}</p>
                    </div>
                ) : (
                    <InstructorRegistrationsTable items={items} onApprove={onApprove} onReject={onReject} />
                )}
            </div>
        </div>
    );
};
