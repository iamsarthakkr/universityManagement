import { PageHeader } from '@/components/common/PageHeader';
import { PlaceholderPanel } from '@/components/common/PlaceholderPanel';

export default function InstructorHomePage() {
    return (
        <>
            <PageHeader
                title="Instructor dashboard"
                description="Instructor landing page for assigned courses, students, and grading."
            />
            <PlaceholderPanel
                title="Instructor module placeholder"
                description="Later add assigned courses, enrollment grading, and finalize-grade actions."
            />
        </>
    );
}
