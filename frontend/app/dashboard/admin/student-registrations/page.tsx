import { PageHeader } from '@/components/common/PageHeader';

export default function StudentRegistrationsPage() {
    return (
        <>
            <PageHeader
                title="Student registration portal"
                description="Manage pending/approved/rejected student registrations here."
            />
            <div className="rounded-2xl border bg-card p-6">
                <p className="text-sm text-muted-foreground">Select a registration status from the sidebar.</p>
            </div>
        </>
    );
}
