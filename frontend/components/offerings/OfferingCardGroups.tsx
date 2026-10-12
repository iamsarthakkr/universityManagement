import { SemesterStatusBadge } from '@/components/semesters/SemesterStatusBadge';
import { formatSemesterName, groupBySemester } from '@/lib/semester';
import { CourseOffering } from '@/types/offering';
import { OfferingCard } from './OfferingCard';

export function OfferingCardGroups({ offerings }: { offerings: CourseOffering[] }) {
    // The server lists oldest semesters first; the latest one matters most here.
    const groups = groupBySemester(offerings, (offering) => offering.semester).reverse();

    return (
        <div className="flex flex-col gap-8">
            {groups.map(({ semester, items }) => (
                <section key={semester.id} aria-label={formatSemesterName(semester)} className="flex flex-col gap-3">
                    <div className="flex items-center gap-3">
                        <h2 className="text-lg font-semibold text-text">{formatSemesterName(semester)}</h2>
                        <SemesterStatusBadge status={semester.status} />
                    </div>
                    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                        {items.map((offering) => (
                            <OfferingCard key={offering.id} offering={offering} />
                        ))}
                    </div>
                </section>
            ))}
        </div>
    );
}
