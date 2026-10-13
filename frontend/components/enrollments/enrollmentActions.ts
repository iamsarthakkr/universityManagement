import { ActionConfig } from '@/components/common/ActionMenu';
import { EnrollmentAction } from '@/types/enrollment';

export const ENROLLMENT_ACTIONS: Record<EnrollmentAction, ActionConfig> = {
    [EnrollmentAction.APPROVE]: { label: 'Approve' },
    [EnrollmentAction.REJECT]: {
        label: 'Reject',
        destructive: true,
        confirm: {
            title: (student) => `Reject ${student}'s request?`,
            description: "This can't be undone.",
            cancelLabel: 'Keep request',
        },
    },
    [EnrollmentAction.CANCEL]: {
        label: 'Cancel request',
        destructive: true,
        confirm: {
            title: (course) => `Cancel your request for ${course}?`,
            description: "This can't be undone.",
            cancelLabel: 'Keep request',
        },
    },
    [EnrollmentAction.DROP]: {
        label: 'Drop course',
        destructive: true,
        confirm: {
            title: (course) => `Drop ${course}?`,
            description: "This can't be undone.",
            cancelLabel: 'Stay enrolled',
        },
    },
};
