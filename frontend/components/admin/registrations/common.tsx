import { ActionConfig } from '@/components/common/ActionMenu';
import { StatusBadge, StatusTone } from '@/components/ui/StatusBadge';
import { RegistrationAction, RegistrationStatus } from '@/types/registration';

const STATUS_TONE: Record<RegistrationStatus, StatusTone> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
};

export const REGISTRATION_ACTIONS: Record<RegistrationAction, ActionConfig> = {
    [RegistrationAction.APPROVE]: {
        label: 'Approve',
        confirm: {
            title: (username) => `Approve ${username}?`,
            description: 'This creates their account and they can log in right away.',
            cancelLabel: 'Not yet',
        },
    },
    [RegistrationAction.REJECT]: {
        label: 'Reject',
        destructive: true,
        confirm: {
            title: (username) => `Reject ${username}?`,
            description: 'They can submit a new request later with the same username and email.',
            cancelLabel: 'Keep request',
        },
    },
};

type StatusProps = {
    status: RegistrationStatus;
};

export const Status = ({ status }: StatusProps) => {
    return <StatusBadge tone={STATUS_TONE[status]}>{status}</StatusBadge>;
};
