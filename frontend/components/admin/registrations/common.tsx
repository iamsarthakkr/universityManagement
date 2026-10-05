import { Button } from '@/components/ui/base/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/base/dropdownMenu';
import { StatusBadge, StatusTone } from '@/components/ui/StatusBadge';
import { Callback1 } from '@/types/common';
import { RegistrationStatus } from '@/types/registration';
import { MoreHorizontalIcon } from 'lucide-react';

const STATUS_TONE: Record<RegistrationStatus, StatusTone> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
};

type ActionsProps = {
    id: number;
    disabled?: boolean;
    onApprove: Callback1<number>;
    onReject: Callback1<number>;
};

export const AdminActions = (props: ActionsProps) => {
    const { id, disabled = false, onApprove, onReject } = props;

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="size-8">
                    <MoreHorizontalIcon className="size-4" />
                    <span className="sr-only">Open menu</span>
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end">
                <DropdownMenuItem disabled={disabled} onClick={() => onApprove(id)}>
                    Approve
                </DropdownMenuItem>
                <DropdownMenuItem disabled={disabled} onClick={() => onReject(id)} variant="destructive">
                    Reject
                </DropdownMenuItem>
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
