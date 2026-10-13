import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { offering } from '@/tests/fixtures';
import { EnrollmentStatus } from '@/types/enrollment';
import { OfferingAction } from '@/types/offering';
import { StudentOfferingCard } from './StudentOfferingCard';

const ENROLLABLE = offering({ allowedActions: [OfferingAction.ENROLL] });

describe('StudentOfferingCard', () => {
    it('requests the offering', async () => {
        const onRequest = vi.fn();
        render(
            <StudentOfferingCard
                offering={ENROLLABLE}
                myStatus={undefined}
                isRequesting={false}
                disabled={false}
                onRequest={onRequest}
            />,
        );

        await userEvent.click(screen.getByRole('button', { name: 'Request enrollment' }));

        expect(onRequest).toHaveBeenCalledWith(ENROLLABLE);
    });

    it('shows progress on the card being requested and disables the button', () => {
        render(
            <StudentOfferingCard
                offering={ENROLLABLE}
                myStatus={undefined}
                isRequesting
                disabled
                onRequest={vi.fn()}
            />,
        );

        expect(screen.getByRole('button', { name: 'Requesting...' })).toBeDisabled();
    });

    it('disables other cards while another request is in flight', () => {
        render(
            <StudentOfferingCard
                offering={ENROLLABLE}
                myStatus={undefined}
                isRequesting={false}
                disabled
                onRequest={vi.fn()}
            />,
        );

        expect(screen.getByRole('button', { name: 'Request enrollment' })).toBeDisabled();
    });

    it('shows the student’s existing status', () => {
        render(
            <StudentOfferingCard
                offering={offering()}
                myStatus={EnrollmentStatus.REJECTED}
                isRequesting={false}
                disabled={false}
                onRequest={vi.fn()}
            />,
        );

        expect(screen.getByText('REJECTED')).toBeInTheDocument();
        expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });
});
