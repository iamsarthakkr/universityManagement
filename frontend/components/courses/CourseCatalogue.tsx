'use client';

import React from 'react';

import { PageHeader } from '@/components/common/PageHeader';
import { useApi } from '@/context/ApiContext';
import { CourseCatalogueGroup } from '@/types/course';

import { DepartmentGroup } from './DepartmentGroup';

export function CourseCatalogue() {
    const api = useApi();

    const [groups, setGroups] = React.useState<CourseCatalogueGroup[]>([]);
    const [isLoading, setIsLoading] = React.useState(true);
    const [error, setError] = React.useState<string | null>(null);

    React.useEffect(() => {
        const fetchData = async () => {
            const res = await api.courses.getCatalogue();

            if (!res.isSuccess || !res.body) {
                setError(res.message || 'Failed to load catalogue.');
            } else {
                setGroups(res.body);
            }

            setIsLoading(false);
        };

        fetchData();
    }, [api]);

    return (
        <>
            <PageHeader title="Course catalogue" description="Browse all available courses grouped by department." />

            <section className="rounded-3xl border border-border bg-white p-6 shadow-soft">
                {isLoading ? (
                    <p className="text-sm text-text-muted">Loading...</p>
                ) : error ? (
                    <p className="text-sm text-destructive">{error}</p>
                ) : groups.length === 0 ? (
                    <p className="text-sm text-text-muted">No courses available yet.</p>
                ) : (
                    <div className="divide-y divide-border">
                        {groups.map((group) => (
                            <DepartmentGroup key={group.department} group={group} />
                        ))}
                    </div>
                )}
            </section>
        </>
    );
}
