import { Button } from '@/components/ui/base/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/base/dropdownMenu';
import { cn } from '@/lib/cn';
import { Callback1 } from '@/types/common';
import { RegistrationStatus } from '@/types/registration';
import { MoreHorizontalIcon } from 'lucide-react';

const statusStyles: Record<RegistrationStatus, string> = {
    PENDING: 'bg-yellow-50 text-yellow-700 ring-yellow-600/20',
    APPROVED: 'bg-green-50 text-green-700 ring-green-600/20',
    REJECTED: 'bg-red-50 text-red-700 ring-red-600/20',
};

type ActionsProps = {
    id: number;
    showActions: boolean;
    onApprove: Callback1<number>;
    onReject: Callback1<number>;
};

export const AdminActions = (props: ActionsProps) => {
    const { id, showActions, onApprove, onReject } = props;

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="size-8">
                    <MoreHorizontalIcon className="size-4" />
                    <span className="sr-only">Open menu</span>
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end">
                <DropdownMenuItem>View details</DropdownMenuItem>

                {showActions && (
                    <>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem onClick={() => onApprove(id)}>Approve</DropdownMenuItem>
                        <DropdownMenuItem onClick={() => onReject(id)} variant="destructive">
                            Reject
                        </DropdownMenuItem>
                    </>
                )}
            </DropdownMenuContent>
        </DropdownMenu>
    );
};

type StatusProps = {
    status: RegistrationStatus;
};

export const Status = ({ status }: StatusProps) => {
    return (
        <span
            className={cn(
                'inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset',
                statusStyles[status],
            )}
        >
            {status}
        </span>
    );
};
