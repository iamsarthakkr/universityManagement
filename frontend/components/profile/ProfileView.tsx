import { DetailItem } from '@/components/common/DetailItem';
import { PageHeader } from '@/components/common/PageHeader';
import { SectionCard } from '@/components/common/SectionCard';
import { fullName } from '@/lib/format';
import { useAppStore } from '@/stores/appStore';

const ROLE_LABEL = { ADMIN: 'Administrator', INSTRUCTOR: 'Instructor', STUDENT: 'Student' } as const;

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
                    {profile && <DetailItem label="Name">{fullName(profile)}</DetailItem>}
                    <DetailItem label="Role">{ROLE_LABEL[user.role]}</DetailItem>
                    <DetailItem label="Username">{user.username}</DetailItem>
                    <DetailItem label="Email">{user.email}</DetailItem>
                    {profile && <DetailItem label="Department">{profile.department.name}</DetailItem>}
                </dl>
            </SectionCard>
        </div>
    );
}
