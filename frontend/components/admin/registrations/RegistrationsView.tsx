import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { DepartmentFilter } from '@/components/common/filters/DepartmentFilter';
import { FilterBar } from '@/components/common/filters/FilterBar';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { StatusTab, StatusTabs } from '@/components/common/StatusTabs';
import { enumParam, idParam, useFilterParams } from '@/hooks/useFilterParams';
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

const FILTERS = {
    status: enumParam(Object.values(RegistrationStatus)),
    departmentId: idParam,
};

const STATUS_TABS: StatusTab<RegistrationStatus>[] = [
    { value: RegistrationStatus.PENDING, label: 'Pending' },
    { value: RegistrationStatus.APPROVED, label: 'Approved' },
    { value: RegistrationStatus.REJECTED, label: 'Rejected' },
];

const KIND_LABEL: Record<RegistrationKind, string> = {
    student: 'Student',
    instructor: 'Instructor',
};

export const RegistrationsView = ({ kind }: { kind: RegistrationKind }) => {
    const api = useApi();
    const registrationApi = React.useMemo(() => getRegistrationApi(api, kind), [api, kind]);
    const queryClient = useQueryClient();

    const [filters, setFilters] = useFilterParams(FILTERS);
    const status = filters.status ?? RegistrationStatus.PENDING;
    const filter = { status, departmentId: filters.departmentId };

    const registrationsQuery = useQuery({
        queryKey: queryKeys.registrations.list(kind, filter),
        queryFn: () => unwrap(registrationApi.list(filter)),
    });
    const items = registrationsQuery.data ?? [];

    const registrationAction = useMutation({
        mutationFn: ({ id, action }: { id: number; action: RegistrationAction }) =>
            unwrap(registrationApi.performAction({ registrationId: id, action })),
        onSuccess: (_, { action }) => {
            toast.success(ACTION_SUCCESS[action]);
            // Approving creates the account, so the people lists change too.
            return Promise.all([
                queryClient.invalidateQueries({ queryKey: queryKeys.registrations.byKind(kind) }),
                queryClient.invalidateQueries({
                    queryKey: kind === 'student' ? queryKeys.students.all : queryKeys.instructors.all,
                }),
            ]);
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

    const emptyMessage = filters.departmentId
        ? `No ${status.toLowerCase()} ${kind} requests in this department.`
        : `No ${status.toLowerCase()} ${kind} registration requests.`;

    return (
        <div className="space-y-6">
            <PageHeader
                title={`${KIND_LABEL[kind]} registrations`}
                description={`Review ${kind} account requests. Approving one creates the account.`}
            />

            <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
                <StatusTabs
                    label="Registration status"
                    tabs={STATUS_TABS}
                    value={status}
                    onChange={(next) => setFilters({ status: next })}
                />
                <FilterBar>
                    <DepartmentFilter
                        value={filters.departmentId}
                        onChange={(departmentId) => setFilters({ departmentId })}
                    />
                </FilterBar>
            </div>

            <SectionCard className="overflow-hidden p-0">
                <QueryState query={registrationsQuery} isEmpty={items.length === 0} emptyMessage={emptyMessage}>
                    <RegistrationsTable kind={kind} items={items} pendingId={pendingId} onAction={onAction} />
                </QueryState>
            </SectionCard>
        </div>
    );
};
