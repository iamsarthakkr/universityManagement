import { ChevronDownIcon } from 'lucide-react';

import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/base/collapsible';
import { Course } from '@/types/course';
import { Department } from '@/types/department';
import { CourseCard } from './CourseCard';

type Props = {
    department: Department;
    courses: Course[];
};

export function DepartmentGroup({ department, courses }: Props) {
    return (
        <Collapsible>
            <CollapsibleTrigger className="flex w-full items-center justify-between py-2 text-left">
                <div className="flex items-center gap-3">
                    <h3 className="font-semibold text-text">{department.name}</h3>
                    <span className="rounded-full bg-surface-muted px-2 py-0.5 text-xs text-text-muted">
                        {courses.length} {courses.length === 1 ? 'course' : 'courses'}
                    </span>
                </div>
                <ChevronDownIcon className="size-4 text-text-muted transition-transform duration-200 [[data-state=open]_&]:rotate-180" />
            </CollapsibleTrigger>
            <CollapsibleContent>
                <div className="mt-2 flex flex-col gap-2 pb-2">
                    {courses.map((course) => (
                        <CourseCard key={course.id} course={course} />
                    ))}
                </div>
            </CollapsibleContent>
        </Collapsible>
    );
}
