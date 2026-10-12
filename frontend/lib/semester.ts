import { Semester, SemesterSummary } from '@/types/semester';

export function formatSemesterName(semester: Pick<Semester, 'term' | 'year'>) {
    const term = semester.term.charAt(0) + semester.term.slice(1).toLowerCase();
    return `${term} ${semester.year}`;
}

export function newestFirst<S extends Pick<Semester, 'startDate'>>(semesters: S[]) {
    return [...semesters].sort((a, b) => b.startDate.localeCompare(a.startDate));
}

export type SemesterGroup<T> = {
    semester: SemesterSummary;
    items: T[];
};

// Keeps the order the items arrive in, so groups follow the server's semester ordering.
export function groupBySemester<T>(items: T[], semesterOf: (item: T) => SemesterSummary): SemesterGroup<T>[] {
    const groups = new Map<number, SemesterGroup<T>>();
    for (const item of items) {
        const semester = semesterOf(item);
        const group = groups.get(semester.id);
        if (group) {
            group.items.push(item);
        } else {
            groups.set(semester.id, { semester, items: [item] });
        }
    }
    return [...groups.values()];
}
