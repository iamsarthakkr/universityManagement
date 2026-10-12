import { ActionConfig } from '@/components/common/ActionMenu';
import { StatusBadge, StatusTone } from '@/components/ui/StatusBadge';
import { RegistrationAction, RegistrationStatus } from '@/types/registration';

const STATUS_TONE: Record<RegistrationStatus, StatusTone> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
};

export const REGISTRATION_ACTIONS: Record<RegistrationAction, ActionConfig> = {
    [RegistrationAction.APPROVE]: { label: 'Approve' },
    [RegistrationAction.REJECT]: { label: 'Reject', destructive: true },
};

type StatusProps = {
    status: RegistrationStatus;
};

export const Status = ({ status }: StatusProps) => {
    return <StatusBadge tone={STATUS_TONE[status]}>{status}</StatusBadge>;
};
