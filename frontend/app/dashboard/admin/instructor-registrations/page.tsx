import { PageHeader } from '@/components/common/PageHeader';

export default function StudentRegistrationsPage() {
    return (
        <>
            <PageHeader
                title="Instructor registration portal"
                description="Manage pending/approved/rejected instructor registrations here."
            />
            <div className="rounded-2xl border bg-card p-6">
                <p className="text-sm text-muted-foreground">Select a registration status from the sidebar.</p>
            </div>
        </>
    );
}
