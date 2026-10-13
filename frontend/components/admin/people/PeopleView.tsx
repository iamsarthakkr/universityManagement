import { useQuery } from '@tanstack/react-query';

import { DepartmentFilter } from '@/components/common/filters/DepartmentFilter';
import { FilterBar } from '@/components/common/filters/FilterBar';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { idParam, useFilterParams } from '@/hooks/useFilterParams';
import { fullName } from '@/lib/format';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { RegistrationKind } from '@/types/registration';

const FILTERS = { departmentId: idParam };

const COPY: Record<RegistrationKind, { title: string; description: string; plural: string }> = {
    student: { title: 'Students', description: 'Everyone with a student account.', plural: 'students' },
    instructor: { title: 'Instructors', description: 'Everyone with an instructor account.', plural: 'instructors' },
};

export function PeopleView({ kind }: { kind: RegistrationKind }) {
    const api = useApi();
    const [filters, setFilters] = useFilterParams(FILTERS);
    const copy = COPY[kind];

    const peopleQuery = useQuery({
        queryKey: kind === 'student' ? queryKeys.students.list(filters) : queryKeys.instructors.list(filters),
        queryFn: () =>
            unwrap(kind === 'student' ? api.students.getStudents(filters) : api.instructors.getInstructors(filters)),
    });
    const people = peopleQuery.data ?? [];

    return (
        <div className="space-y-6">
            <PageHeader title={copy.title} description={copy.description} />
            <FilterBar>
                <DepartmentFilter
                    value={filters.departmentId}
                    onChange={(departmentId) => setFilters({ departmentId })}
                />
            </FilterBar>
            <SectionCard className="overflow-hidden p-0">
                <QueryState
                    query={peopleQuery}
                    isEmpty={people.length === 0}
                    emptyMessage={
                        filters.departmentId ? `No ${copy.plural} in this department.` : `No ${copy.plural} yet.`
                    }
                >
                    <Table>
                        <TableHeader className="bg-surface-muted">
                            <TableRow>
                                <TableHead>Name</TableHead>
                                <TableHead>Email</TableHead>
                                <TableHead>Department</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {people.map((person) => (
                                <TableRow key={person.id}>
                                    <TableCell className="font-semibold">{fullName(person)}</TableCell>
                                    <TableCell>{person.email}</TableCell>
                                    <TableCell>{person.department.name}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </QueryState>
            </SectionCard>
        </div>
    );
}
