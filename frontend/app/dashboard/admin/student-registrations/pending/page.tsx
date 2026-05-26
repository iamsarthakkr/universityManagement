import { StudentRegistrationsView } from '@/components/admin/registrations/StudentRegistrationsView';
import { RegistrationStatus } from '@/types/registration';

export default function PendingStudentRegistrationsPage() {
    return (
        <StudentRegistrationsView
            title="Pending Student Registrations"
            description="Review pending student registration requests."
            placeholder="Pending registrations table placeholder."
            status={RegistrationStatus.PENDING}
        />
    );
}
