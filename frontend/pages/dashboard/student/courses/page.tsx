import { ComingSoonPanel } from '@/components/common/ComingSoonPanel';
import { PageHeader } from '@/components/common/PageHeader';

export default function StudentCoursesPage() {
    return (
        <>
            <PageHeader title="My courses" description="Courses you are enrolled in." />
            <ComingSoonPanel description="Your enrolled courses will appear here." />
        </>
    );
}
