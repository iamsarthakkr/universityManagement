import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { OfferingSeats } from './OfferingSeats';

describe('OfferingSeats', () => {
    it('shows the seats left and how full the offering is', () => {
        render(<OfferingSeats enrolled={12} capacity={30} seatsAvailable={18} />);

        expect(screen.getByText('18 seats left')).toBeInTheDocument();
        expect(screen.getByText('12 / 30 enrolled')).toBeInTheDocument();
        expect(screen.getByRole('meter', { name: 'Seats taken' })).toHaveAttribute('aria-valuenow', '12');
    });

    it('says when the offering is full', () => {
        render(<OfferingSeats enrolled={30} capacity={30} seatsAvailable={0} />);

        expect(screen.getByText('Full')).toBeInTheDocument();
    });
});
