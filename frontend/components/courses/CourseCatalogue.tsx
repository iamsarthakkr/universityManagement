import { useQuery } from '@tanstack/react-query';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SectionCard } from '@/components/common/SectionCard';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { Course } from '@/types/course';
import { Department } from '@/types/department';
import { DepartmentGroup } from './DepartmentGroup';

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

export function CourseCatalogue() {
    const api = useApi();

    const coursesQuery = useQuery({
        queryKey: queryKeys.courses.list(),
        queryFn: () => unwrap(api.courses.getCourses({})),
        select: groupByDepartment,
    });

    const groups = coursesQuery.data ?? [];

    return (
        <>
            <PageHeader title="Course catalogue" description="Browse all available courses grouped by department." />
            <SectionCard>
                <QueryState query={coursesQuery} isEmpty={groups.length === 0} emptyMessage="No courses available yet.">
                    <div className="divide-y divide-border">
                        {groups.map((group) => (
                            <DepartmentGroup
                                key={group.department.id}
                                department={group.department}
                                courses={group.courses}
                            />
                        ))}
                    </div>
                </QueryState>
            </SectionCard>
        </>
    );
}
