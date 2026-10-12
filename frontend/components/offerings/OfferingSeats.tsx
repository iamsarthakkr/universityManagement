import { cn } from '@/lib/cn';
import { CourseOffering } from '@/types/offering';

type Props = Pick<CourseOffering, 'enrolled' | 'capacity' | 'seatsAvailable'>;

export function OfferingSeats({ enrolled, capacity, seatsAvailable }: Props) {
    const isFull = seatsAvailable === 0;
    const percent = capacity > 0 ? Math.min(100, Math.round((enrolled / capacity) * 100)) : 0;

    return (
        <div className="flex min-w-28 flex-col gap-1">
            <span className={cn('text-sm', isFull ? 'font-semibold text-destructive' : 'text-text')}>
                {isFull ? 'Full' : `${seatsAvailable} seats left`}
            </span>
            <div
                className="h-1.5 w-full overflow-hidden rounded-full bg-surface-muted"
                role="meter"
                aria-label="Seats taken"
                aria-valuemin={0}
                aria-valuemax={capacity}
                aria-valuenow={enrolled}
            >
                <div
                    className={cn('h-full', isFull ? 'bg-destructive' : 'bg-brand')}
                    style={{ width: `${percent}%` }}
                />
            </div>
            <span className="text-xs text-text-muted">
                {enrolled} / {capacity} enrolled
            </span>
        </div>
    );
}
