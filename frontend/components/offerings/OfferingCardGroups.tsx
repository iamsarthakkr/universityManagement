import { SemesterSections } from '@/components/semesters/SemesterSections';
import { groupBySemester } from '@/lib/semester';
import { CourseOffering } from '@/types/offering';
import { OfferingCard } from './OfferingCard';

export function OfferingCardGroups({ offerings }: { offerings: CourseOffering[] }) {
    // The server lists oldest semesters first; the latest one matters most here.
    const groups = groupBySemester(offerings, (offering) => offering.semester).reverse();

    return (
        <SemesterSections
            groups={groups}
            itemKey={(offering) => offering.id}
            renderItem={(offering) => <OfferingCard offering={offering} />}
        />
    );
}
