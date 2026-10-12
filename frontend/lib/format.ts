import { CourseOfferingSummary } from '@/types/offering';
import { formatSemesterName } from './semester';

type Named = {
    firstName: string;
    lastName?: string | null;
};

export function fullName({ firstName, lastName }: Named) {
    return lastName ? `${firstName} ${lastName}` : firstName;
}

export function formatOffering({
    course,
    section,
    semester,
}: Pick<CourseOfferingSummary, 'course' | 'section' | 'semester'>) {
    return `${course.code} · Section ${section} · ${formatSemesterName(semester)}`;
}
