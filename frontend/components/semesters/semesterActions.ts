import { ActionConfig } from '@/components/common/ActionMenu';
import { SemesterAction } from '@/types/semester';

export const SEMESTER_ACTIONS: Record<SemesterAction, ActionConfig> = {
    [SemesterAction.ACTIVATE]: { label: 'Activate' },
    [SemesterAction.COMPLETE]: { label: 'Mark as completed' },
    [SemesterAction.CANCEL]: {
        label: 'Cancel semester',
        destructive: true,
        confirm: {
            title: (semester) => `Cancel ${semester}?`,
            description: "This can't be undone.",
            cancelLabel: 'Keep semester',
        },
    },
};
