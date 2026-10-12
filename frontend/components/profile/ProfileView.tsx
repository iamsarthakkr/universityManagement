import { ReactNode } from 'react';

import { PageHeader } from '@/components/common/PageHeader';
import { SectionCard } from '@/components/common/SectionCard';
import { fullName } from '@/lib/format';
import { useAppStore } from '@/stores/appStore';

const ROLE_LABEL = { ADMIN: 'Administrator', INSTRUCTOR: 'Instructor', STUDENT: 'Student' } as const;

function Detail({ label, children }: { label: string; children: ReactNode }) {
    return (
        <div className="flex flex-col gap-1">
            <dt className="text-xs font-medium uppercase tracking-wide text-text-muted">{label}</dt>
            <dd className="text-sm text-text">{children}</dd>
        </div>
    );
}

export function ProfileView() {
    const user = useAppStore((state) => state.user);

    if (!user) {
        return null;
    }

    const { profile } = user;

    return (
        <div className="space-y-6">
            <PageHeader title="Profile" description="Your account details." />
            <SectionCard>
                <dl className="grid gap-6 sm:grid-cols-2">
                    {profile && <Detail label="Name">{fullName(profile)}</Detail>}
                    <Detail label="Role">{ROLE_LABEL[user.role]}</Detail>
                    <Detail label="Username">{user.username}</Detail>
                    <Detail label="Email">{user.email}</Detail>
                    {profile && <Detail label="Department">{profile.department.name}</Detail>}
                </dl>
            </SectionCard>
        </div>
    );
}
