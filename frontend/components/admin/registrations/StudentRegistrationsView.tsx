'use client';

import { PageHeader } from '@/components/common/PageHeader';
import { RegistrationStatus, StudentRegistrationResponse } from '@/types/registration';
import React from 'react';
import { useApi } from '@/context/ApiContext';
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

    const fetchData = React.useCallback(async () => {
        setIsLoading(true);

        const res = await api.admin.getStudentRegistrations(status);
        if (!res.isSuccess || !res.body) {
            setError(res.message);
            return;
        }
        setItems(res.body);

        setIsLoading(false);
    }, []);

    const onApprove = React.useCallback(
        async (item: StudentRegistrationResponse) => {
            const res = await api.admin.approveStudentRegistration(item.id);
            if (res.isSuccess && res.body) {
                await fetchData();
                toast.success('Request approved successfully!');
            } else {
                toast.error('Failed to approve request');
            }
        },
        [api],
    );

    const onReject = React.useCallback(async (item: StudentRegistrationResponse) => {
        const res = await api.admin.rejectStudentRegistration(item.id);
        if (res.isSuccess && res.body) {
            await fetchData();
            toast.success('Request rejected successfully!');
        } else {
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
                    <StudentRegistrationsTable items={items} onApprove={onApprove} onReject={onReject} />
                )}
            </div>
        </div>
    );
};
