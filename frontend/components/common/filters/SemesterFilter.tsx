import { useQuery } from '@tanstack/react-query';

import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { formatSemesterName, newestFirst } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { Semester } from '@/types/semester';
import { FilterSelect } from './FilterSelect';

function toOptions(semesters: Semester[]) {
    return newestFirst(semesters).map((semester) => ({ value: semester.id, label: formatSemesterName(semester) }));
}

type Props = {
    value: number | undefined;
    onChange: (semesterId: number | undefined) => void;
};

export function SemesterFilter({ value, onChange }: Props) {
    const api = useApi();
    const semestersQuery = useQuery({
        queryKey: queryKeys.semesters.list,
        queryFn: () => unwrap(api.semesters.getSemesters()),
        select: toOptions,
    });

    return (
        <FilterSelect
            label="Semester"
            allLabel={semestersQuery.isError ? 'Semesters unavailable' : 'All semesters'}
            value={value}
            options={semestersQuery.data ?? []}
            onChange={onChange}
            disabled={!semestersQuery.isSuccess}
        />
    );
}
