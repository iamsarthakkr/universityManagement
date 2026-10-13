import { Navigate, useParams } from 'react-router';

import { enumParam } from '@/hooks/useFilterParams';
import { RegistrationKind, RegistrationStatus } from '@/types/registration';

const parseStatus = enumParam(Object.values(RegistrationStatus));

// Registration status used to be a path segment; keeps old bookmarks working.
export function LegacyStatusRedirect({ kind }: { kind: RegistrationKind }) {
    const status = parseStatus(useParams().status ?? '');
    const path = `/dashboard/admin/${kind}-registrations`;

    return <Navigate to={status ? `${path}?status=${status}` : path} replace />;
}
