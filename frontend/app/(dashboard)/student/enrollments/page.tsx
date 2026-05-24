import { PageHeader } from '@/components/common/PageHeader';
import { PlaceholderPanel } from '@/components/common/PlaceholderPanel';

export default function StudentEnrollmentsPage() {
    return (
        <>
            <PageHeader
                title="My enrollments"
                description="Placeholder for enrollment status, marks, and derived grades."
            />
            <PlaceholderPanel
                title="Enrollment table pending"
                description="This can later consume /students/{id}/enrollments or a current-user endpoint."
            />
        </>
    );
}
