import { Navigate, useParams } from 'react-router';

import { RegistrationsView } from '@/components/admin/registrations/RegistrationsView';
import { parseRegistrationStatus } from '@/types/registration';

export default function InstructorRegistrationsPage() {
    const status = parseRegistrationStatus(useParams().status);

    if (!status) {
        return <Navigate to="/dashboard/admin/instructor-registrations/pending" replace />;
    }

    return <RegistrationsView key={status} kind="instructor" status={status} />;
}
