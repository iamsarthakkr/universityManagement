import { StudentRegistrationsView } from '@/components/admin/registrations/StudentRegistrationsView';
import { RegistrationStatus } from '@/types/registration';

export default function ApprovedStudentRegistrationsPage() {
    return (
        <StudentRegistrationsView
            title="Approved Student Registrations"
            description="Approved student registration requests."
            placeholder="No Approved student registration requests found."
            status={RegistrationStatus.APPROVED}
        />
    );
}
