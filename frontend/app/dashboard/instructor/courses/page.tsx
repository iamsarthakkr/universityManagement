import { PageHeader } from '@/components/common/PageHeader';
import { PlaceholderPanel } from '@/components/common/PlaceholderPanel';

export default function InstructorCoursesPage() {
    return (
        <>
            <PageHeader title="Assigned courses" description="Placeholder for courses assigned to the instructor." />
            <PlaceholderPanel
                title="Assigned courses pending"
                description="Wire this to instructor-course APIs when backend endpoints are added."
            />
        </>
    );
}
