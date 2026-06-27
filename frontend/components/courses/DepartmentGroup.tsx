'use client';

import { ChevronDownIcon } from 'lucide-react';

import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/base/collapsible';
import { CourseCatalogueGroup } from '@/types/course';

import { CourseCard } from './CourseCard';

export function DepartmentGroup({ group }: { group: CourseCatalogueGroup }) {
    return (
        <Collapsible>
            <CollapsibleTrigger className="flex w-full items-center justify-between py-2 text-left">
                <div className="flex items-center gap-3">
                    <h3 className="font-semibold text-text">{group.department}</h3>
                    <span className="rounded-full bg-surface-muted px-2 py-0.5 text-xs text-text-muted">
                        {group.courseList.length} {group.courseList.length === 1 ? 'course' : 'courses'}
                    </span>
                </div>
                <ChevronDownIcon className="size-4 text-text-muted transition-transform duration-200 [[data-state=open]_&]:rotate-180" />
            </CollapsibleTrigger>

            <CollapsibleContent>
                <div className="mt-2 flex flex-col gap-2 pb-2">
                    {group.courseList.map((course) => (
                        <CourseCard key={course.courseId} course={course} />
                    ))}
                </div>
            </CollapsibleContent>
        </Collapsible>
    );
}
