import { StatusBadge, StatusTone } from '@/components/ui/StatusBadge';
import { SemesterStatus } from '@/types/semester';

const SEMESTER_STATUS_TONE: Record<SemesterStatus, StatusTone> = {
    PLANNED: 'info',
    ACTIVE: 'success',
    COMPLETED: 'neutral',
    CANCELLED: 'danger',
};

export function SemesterStatusBadge({ status }: { status: SemesterStatus }) {
    return <StatusBadge tone={SEMESTER_STATUS_TONE[status]}>{status}</StatusBadge>;
}
