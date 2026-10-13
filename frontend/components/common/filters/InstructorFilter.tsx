import { useQuery } from '@tanstack/react-query';

import { fullName } from '@/lib/format';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { Instructor } from '@/types/instructor';
import { FilterSelect } from './FilterSelect';

function toOptions(instructors: Instructor[]) {
    return instructors.map((instructor) => ({ value: instructor.id, label: fullName(instructor) }));
}

type Props = {
    value: number | undefined;
    onChange: (instructorId: number | undefined) => void;
};

export function InstructorFilter({ value, onChange }: Props) {
    const api = useApi();
    const instructorsQuery = useQuery({
        queryKey: queryKeys.instructors.list(),
        queryFn: () => unwrap(api.instructors.getInstructors({})),
        select: toOptions,
    });

    return (
        <FilterSelect
            label="Instructor"
            allLabel={instructorsQuery.isError ? 'Instructors unavailable' : 'All instructors'}
            value={value}
            options={instructorsQuery.data ?? []}
            onChange={onChange}
            disabled={!instructorsQuery.isSuccess}
        />
    );
}
