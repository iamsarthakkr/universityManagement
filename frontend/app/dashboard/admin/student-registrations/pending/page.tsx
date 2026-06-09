import { StudentRegistrationsView } from '@/components/admin/registrations/StudentRegistrationsView';
import { RegistrationStatus } from '@/types/registration';

export default function PendingStudentRegistrationsPage() {
    return (
        <StudentRegistrationsView
            title="Pending Student Registrations"
            description="Review pending student registration requests."
            placeholder="No Pending student registration requests found."
            status={RegistrationStatus.PENDING}
        />
    );
}
