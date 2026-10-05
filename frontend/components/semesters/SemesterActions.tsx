import { MoreHorizontalIcon } from 'lucide-react';

import { Button } from '@/components/ui/base/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/base/dropdownMenu';
import { Semester, SemesterStatus } from '@/types/semester';

export const TRANSITION_LABEL: Record<SemesterStatus, string> = {
    [SemesterStatus.PLANNED]: 'Move to planned',
    [SemesterStatus.ACTIVE]: 'Activate',
    [SemesterStatus.COMPLETED]: 'Mark as completed',
    [SemesterStatus.CANCELLED]: 'Cancel semester',
};

type Props = {
    semester: Semester;
    disabled: boolean;
    onTransition: (semester: Semester, status: SemesterStatus) => void;
};

export function SemesterActions({ semester, disabled, onTransition }: Props) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="size-8" disabled={disabled}>
                    <MoreHorizontalIcon className="size-4" />
                    <span className="sr-only">Open menu</span>
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end">
                {semester.allowedTransitions.map((status) => (
                    <DropdownMenuItem
                        key={status}
                        variant={status === SemesterStatus.CANCELLED ? 'destructive' : 'default'}
                        onClick={() => onTransition(semester, status)}
                    >
                        {TRANSITION_LABEL[status]}
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
