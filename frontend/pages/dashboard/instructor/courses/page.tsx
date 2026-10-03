import { ComingSoonPanel } from '@/components/common/ComingSoonPanel';
import { PageHeader } from '@/components/common/PageHeader';

export default function InstructorCoursesPage() {
    return (
        <>
            <PageHeader title="Assigned courses" description="Courses you are assigned to teach." />
            <ComingSoonPanel description="Courses assigned to you will appear here." />
        </>
    );
}
