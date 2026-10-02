import { Navigate, useParams } from 'react-router';

import { RegistrationsView } from '@/components/admin/registrations/RegistrationsView';
import { parseRegistrationStatus } from '@/types/registration';

export default function StudentRegistrationsPage() {
    const status = parseRegistrationStatus(useParams().status);

    if (!status) {
        return <Navigate to="/dashboard/admin/student-registrations/pending" replace />;
    }

    return <RegistrationsView kind="student" status={status} />;
}
