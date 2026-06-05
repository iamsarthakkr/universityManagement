'use client';

import { PageHeader } from '@/components/common/PageHeader';
import { RegistrationStatus, StudentRegistrationResponse } from '@/types/registration';
import React from 'react';
import { useApi } from '@/context/ApiContext';
import { StudentRegistrationsTable } from './StudentRegistrationsTable';

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

    React.useEffect(() => {
        const asyncFetch = async () => {
            setIsLoading(true);

            const res = await api.admin.getStudentRegistrations(status);
            if (!res.isSuccess || !res.body) {
                setError(res.message);
                return;
            }
            setItems(res.body);

            setIsLoading(false);
        };
        asyncFetch();
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
                        <p className="text-sm text-muted-foreground">No pending student registration requests</p>
                    </div>
                ) : (
                    <StudentRegistrationsTable items={items} />
                )}
            </div>
        </div>
    );
};
