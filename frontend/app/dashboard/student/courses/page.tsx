import { PageHeader } from '@/components/common/PageHeader';
import { PlaceholderPanel } from '@/components/common/PlaceholderPanel';

export default function StudentCoursesPage() {
    return (
        <>
            <PageHeader title="Student courses" description="Placeholder for available courses and enrolled courses." />
            <PlaceholderPanel
                title="Courses UI pending"
                description="Add course cards, enroll buttons, and enrollment status once APIs are available."
            />
        </>
    );
}
