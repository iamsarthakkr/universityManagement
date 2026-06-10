import { InstructorRegistrationsView } from '@/components/admin/registrations/InstructorRegistrationsView';
import { RegistrationStatus } from '@/types/registration';

export default function ApprovedInstructorRegistrationsPage() {
    return (
        <InstructorRegistrationsView
            title="Approved Instructor Registrations"
            description="Approved instructor registration requests."
            placeholder="No Approved instructor registration requests found."
            status={RegistrationStatus.APPROVED}
        />
    );
}
