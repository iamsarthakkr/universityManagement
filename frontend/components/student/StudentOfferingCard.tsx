import { EnrollmentStatusBadge } from '@/components/enrollments/EnrollmentStatusBadge';
import { OfferingSeats } from '@/components/offerings/OfferingSeats';
import { Button } from '@/components/ui/base/button';
import { formatCredits, fullName } from '@/lib/format';
import { EnrollmentStatus } from '@/types/enrollment';
import { CourseOffering, OfferingAction } from '@/types/offering';

type Props = {
    offering: CourseOffering;
    myStatus: EnrollmentStatus | undefined;
    isRequesting: boolean;
    disabled: boolean;
    onRequest: (offering: CourseOffering) => void;
};

export function StudentOfferingCard({ offering, myStatus, isRequesting, disabled, onRequest }: Props) {
    const canEnroll = offering.allowedActions.includes(OfferingAction.ENROLL);

    return (
        <article
            aria-label={`${offering.course.code} section ${offering.section}`}
            className="flex w-full flex-col gap-4 rounded-2xl border border-border bg-surface p-5 shadow-soft"
        >
            <div className="flex items-start justify-between gap-3">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-wide text-text-muted">
                        {offering.course.code} · Section {offering.section}
                    </p>
                    <h3 className="mt-0.5 font-semibold text-text">{offering.course.title}</h3>
                    <p className="mt-1 text-sm text-text-muted">
                        {fullName(offering.instructor)} · {formatCredits(offering.course.credits)}
                    </p>
                </div>
                {myStatus && <EnrollmentStatusBadge status={myStatus} />}
            </div>
            <div className="mt-auto flex flex-wrap items-end justify-between gap-3">
                <OfferingSeats
                    enrolled={offering.enrolled}
                    capacity={offering.capacity}
                    seatsAvailable={offering.seatsAvailable}
                />
                {canEnroll && (
                    <Button size="sm" disabled={disabled} onClick={() => onRequest(offering)}>
                        {isRequesting ? 'Requesting...' : 'Request enrollment'}
                    </Button>
                )}
            </div>
        </article>
    );
}
