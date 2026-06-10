import { InstructorRegistrationsView } from '@/components/admin/registrations/InstructorRegistrationsView';
import { RegistrationStatus } from '@/types/registration';

export default function PendingInstructorRegistrationsPage() {
    return (
        <InstructorRegistrationsView
            title="Pending Instructor Registrations"
            description="Review pending instructor registration requests."
            placeholder="No Pending instructor registration requests found."
            status={RegistrationStatus.PENDING}
        />
    );
}
