import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { EnrollmentStatusBadge } from '@/components/enrollments/EnrollmentStatusBadge';
import { SemesterStatusBadge } from '@/components/semesters/SemesterStatusBadge';
import { EnrollmentStatus } from '@/types/enrollment';
import { SemesterStatus } from '@/types/semester';

describe('status badges', () => {
    it.each(Object.values(SemesterStatus))('renders semester status %s', (status) => {
        render(<SemesterStatusBadge status={status} />);

        expect(screen.getByText(status)).toBeInTheDocument();
    });

    it.each(Object.values(EnrollmentStatus))('renders enrollment status %s', (status) => {
        render(<EnrollmentStatusBadge status={status} />);

        expect(screen.getByText(status)).toBeInTheDocument();
    });

    it('gives each tone its own styling', () => {
        render(
            <>
                <EnrollmentStatusBadge status={EnrollmentStatus.PENDING} />
                <EnrollmentStatusBadge status={EnrollmentStatus.ENROLLED} />
            </>,
        );

        expect(screen.getByText('PENDING').className).not.toBe(screen.getByText('ENROLLED').className);
    });
});
