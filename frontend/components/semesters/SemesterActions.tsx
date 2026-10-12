import { MoreHorizontalIcon } from 'lucide-react';

import { Button } from '@/components/ui/base/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/base/dropdownMenu';
import { Semester, SemesterAction } from '@/types/semester';

export const ACTION_LABEL: Record<SemesterAction, string> = {
    [SemesterAction.ACTIVATE]: 'Activate',
    [SemesterAction.COMPLETE]: 'Mark as completed',
    [SemesterAction.CANCEL]: 'Cancel semester',
};

type Props = {
    semester: Semester;
    disabled: boolean;
    onAction: (semester: Semester, action: SemesterAction) => void;
};

export function SemesterActions({ semester, disabled, onAction }: Props) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="size-8" disabled={disabled}>
                    <MoreHorizontalIcon className="size-4" />
                    <span className="sr-only">Open menu</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                {semester.allowedActions.map((action) => (
                    <DropdownMenuItem
                        key={action}
                        variant={action === SemesterAction.CANCEL ? 'destructive' : 'default'}
                        onClick={() => onAction(semester, action)}
                    >
                        {ACTION_LABEL[action]}
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
