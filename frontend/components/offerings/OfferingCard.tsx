import { ChevronRightIcon } from 'lucide-react';
import { Link } from 'react-router';

import { StatusBadge } from '@/components/ui/StatusBadge';
import { CourseOffering } from '@/types/offering';
import { OfferingSeats } from './OfferingSeats';

export function OfferingCard({ offering }: { offering: CourseOffering }) {
    return (
        <Link
            to={`/dashboard/offerings/${offering.id}`}
            aria-label={`${offering.course.code} section ${offering.section}`}
            className="group flex w-full flex-col gap-4 rounded-2xl border border-border bg-surface p-5 shadow-soft transition-colors hover:border-brand/40 hover:bg-brand-soft/40"
        >
            <div className="flex items-start justify-between gap-3">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-wide text-text-muted">
                        {offering.course.code} · Section {offering.section}
                    </p>
                    <p className="mt-0.5 font-semibold text-text">{offering.course.title}</p>
                </div>
                <ChevronRightIcon className="mt-0.5 size-4 shrink-0 text-text-muted transition-transform group-hover:translate-x-0.5" />
            </div>
            <div className="flex items-end justify-between gap-3">
                <OfferingSeats
                    enrolled={offering.enrolled}
                    capacity={offering.capacity}
                    seatsAvailable={offering.seatsAvailable}
                />
                {offering.semester.isRegistrationOpen && <StatusBadge tone="success">Registration open</StatusBadge>}
            </div>
        </Link>
    );
}
