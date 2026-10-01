import { StudentRegistrationsView } from '@/components/admin/registrations/StudentRegistrationsView';
import { RegistrationStatus } from '@/types/registration';

export default function RejectedStudentRegistrationsPage() {
    return (
        <StudentRegistrationsView
            title="Rejected Student Registrations"
            description="Rejected student registration requests."
            placeholder="No Rejected student registration requests found."
            status={RegistrationStatus.REJECTED}
        />
    );
}
