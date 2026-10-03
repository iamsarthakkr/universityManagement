import { ComingSoonPanel } from '@/components/common/ComingSoonPanel';
import { PageHeader } from '@/components/common/PageHeader';

export default function StudentEnrollmentsPage() {
    return (
        <>
            <PageHeader title="Enrollments" description="Your enrollment status, marks, and grades." />
            <ComingSoonPanel description="Your enrollments, marks, and grades will appear here." />
        </>
    );
}
