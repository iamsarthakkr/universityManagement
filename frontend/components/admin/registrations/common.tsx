import { MoreHorizontalIcon } from 'lucide-react';

import { Button } from '@/components/ui/base/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/base/dropdownMenu';
import { StatusBadge, StatusTone } from '@/components/ui/StatusBadge';
import { RegistrationAction, RegistrationStatus } from '@/types/registration';

const STATUS_TONE: Record<RegistrationStatus, StatusTone> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
};

const ACTION_LABEL: Record<RegistrationAction, string> = {
    [RegistrationAction.APPROVE]: 'Approve',
    [RegistrationAction.REJECT]: 'Reject',
};

type ActionsProps = {
    id: number;
    actions: RegistrationAction[];
    disabled?: boolean;
    onAction: (id: number, action: RegistrationAction) => void;
};

export const AdminActions = ({ id, actions, disabled = false, onAction }: ActionsProps) => {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="size-8" disabled={disabled}>
                    <MoreHorizontalIcon className="size-4" />
                    <span className="sr-only">Open menu</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                {actions.map((action) => (
                    <DropdownMenuItem
                        key={action}
                        variant={action === RegistrationAction.REJECT ? 'destructive' : 'default'}
                        onClick={() => onAction(id, action)}
                    >
                        {ACTION_LABEL[action]}
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
};

type StatusProps = {
    status: RegistrationStatus;
};

export const Status = ({ status }: StatusProps) => {
    return <StatusBadge tone={STATUS_TONE[status]}>{status}</StatusBadge>;
};
