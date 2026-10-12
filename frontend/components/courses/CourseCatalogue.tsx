import { useQuery } from '@tanstack/react-query';
import { PlusIcon, SearchIcon } from 'lucide-react';
import { Link } from 'react-router';

import { DepartmentFilter } from '@/components/common/filters/DepartmentFilter';
import { FilterBar } from '@/components/common/filters/FilterBar';
import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { Button } from '@/components/ui/base/button';
import { Input } from '@/components/ui/base/input';
import { idParam, textParam, useFilterParams } from '@/hooks/useFilterParams';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { useAppStore } from '@/stores/appStore';
import { Course } from '@/types/course';
import { Department } from '@/types/department';
import { DepartmentGroup } from './DepartmentGroup';

const FILTERS = { departmentId: idParam, q: textParam };

type CourseGroup = {
    department: Department;
    courses: Course[];
};

// The server returns a flat list; grouping by department is only how the catalogue is displayed.
function groupByDepartment(courses: Course[]): CourseGroup[] {
    const groups = new Map<number, CourseGroup>();
    for (const course of courses) {
        const group = groups.get(course.department.id);
        if (group) {
            group.courses.push(course);
        } else {
            groups.set(course.department.id, { department: course.department, courses: [course] });
        }
    }

    return [...groups.values()].sort((a, b) => a.department.name.localeCompare(b.department.name));
}

function matches(course: Course, search: string) {
    return course.code.toLowerCase().includes(search) || course.title.toLowerCase().includes(search);
}

export function CourseCatalogue() {
    const api = useApi();
    const isAdmin = useAppStore((state) => state.user?.role === 'ADMIN');
    const [filters, setFilters] = useFilterParams(FILTERS);
    const serverFilter = { departmentId: filters.departmentId };
    const search = filters.q?.trim().toLowerCase() ?? '';

    const coursesQuery = useQuery({
        queryKey: queryKeys.courses.list(serverFilter),
        queryFn: () => unwrap(api.courses.getCourses(serverFilter)),
    });

    const courses = (coursesQuery.data ?? []).filter((course) => matches(course, search));
    const groups = groupByDepartment(courses);
    const isNarrowed = search !== '' || filters.departmentId !== undefined;

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <PageHeader
                    title="Course catalogue"
                    description="Browse all available courses grouped by department."
                />
                {isAdmin && (
                    <Button asChild>
                        <Link to="/dashboard/courses/new">
                            <PlusIcon />
                            New course
                        </Link>
                    </Button>
                )}
            </div>
            <FilterBar>
                <label className="flex flex-col gap-1 text-xs font-medium text-text-muted sm:w-64">
                    Search
                    <div className="relative">
                        <SearchIcon className="pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-text-muted" />
                        <Input
                            type="search"
                            placeholder="Code or title"
                            className="pl-8 text-text"
                            value={filters.q ?? ''}
                            onChange={(event) => setFilters({ q: textParam(event.target.value) })}
                        />
                    </div>
                </label>
                <DepartmentFilter
                    value={filters.departmentId}
                    onChange={(departmentId) => setFilters({ departmentId })}
                />
            </FilterBar>
            <SectionCard>
                <QueryState
                    query={coursesQuery}
                    isEmpty={groups.length === 0}
                    emptyMessage={isNarrowed ? 'No courses match your search.' : 'No courses available yet.'}
                >
                    <div className="divide-y divide-border">
                        {groups.map((group) => (
                            <DepartmentGroup
                                // Remount when narrowing starts or stops so the groups open or close with it.
                                key={`${group.department.id}-${isNarrowed}`}
                                department={group.department}
                                courses={group.courses}
                                defaultOpen={isNarrowed}
                            />
                        ))}
                    </div>
                </QueryState>
            </SectionCard>
        </div>
    );
}
