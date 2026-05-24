import { PageHeader } from '@/components/common/PageHeader';
import { PlaceholderPanel } from '@/components/common/PlaceholderPanel';

export default function StudentHomePage() {
    return (
        <>
            <PageHeader
                title="Student dashboard"
                description="Student landing page for profile, enrollments, courses, marks, and grades."
            />
            <PlaceholderPanel
                title="Student module placeholder"
                description="Later add course browsing, enrollment creation, enrollment status, marks, and grade display here."
            />
        </>
    );
}
