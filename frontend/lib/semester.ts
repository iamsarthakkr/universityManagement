import { Semester } from '@/types/semester';

export function formatSemesterName(semester: Pick<Semester, 'term' | 'year'>) {
    const term = semester.term.charAt(0) + semester.term.slice(1).toLowerCase();
    return `${term} ${semester.year}`;
}
