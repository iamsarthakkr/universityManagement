import { StatusBadge, StatusTone } from '@/components/ui/StatusBadge';
import { EnrollmentStatus } from '@/types/enrollment';

const ENROLLMENT_STATUS_TONE: Record<EnrollmentStatus, StatusTone> = {
    PENDING: 'warning',
    ENROLLED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'neutral',
    DROPPED: 'neutral',
};

export function EnrollmentStatusBadge({ status }: { status: EnrollmentStatus }) {
    return <StatusBadge tone={ENROLLMENT_STATUS_TONE[status]}>{status}</StatusBadge>;
}
