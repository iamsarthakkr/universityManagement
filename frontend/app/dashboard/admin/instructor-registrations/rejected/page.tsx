import { InstructorRegistrationsView } from '@/components/admin/registrations/InstructorRegistrationsView';
import { RegistrationStatus } from '@/types/registration';

export default function RejectedInstructorRegistrationsPage() {
    return (
        <InstructorRegistrationsView
            title="Rejected Instructor Registrations"
            description="Rejected instructor registration requests."
            placeholder="No Rejected instructor registration requests found."
            status={RegistrationStatus.REJECTED}
        />
    );
}
