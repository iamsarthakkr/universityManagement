import { PageHeader } from '@/components/common/PageHeader';

export default function StudentRegistrationsPage() {
    return (
        <>
            <PageHeader
                title="Pending student registrations"
                description="Dummy table for /admin/student-registrations style flow. Later wire approve/reject actions to your backend."
            />{' '}
            <div className="rounded-2xl border bg-card p-6">
                <p className="text-sm text-muted-foreground">Select a registration status from the sidebar.</p>
            </div>
        </>
    );
}
