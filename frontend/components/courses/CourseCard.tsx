import { ChevronDownIcon, ChevronUpIcon } from 'lucide-react';
import React from 'react';

import { Button } from '@/components/ui/base/button';
import { Course } from '@/types/course';

export function CourseCard({ course }: { course: Course }) {
    const [expanded, setExpanded] = React.useState(false);

    return (
        <div className="rounded-2xl border border-border bg-surface-muted p-4">
            <div className="flex items-start justify-between gap-4">
                <div>
                    <span className="text-xs font-semibold uppercase tracking-wide text-text-muted">
                        {`${course.code} · ${course.credits} ${course.credits === 1 ? 'credit' : 'credits'}`}
                    </span>
                    <h4 className="mt-0.5 font-semibold text-text">{course.title}</h4>
                </div>
                <Button
                    variant="ghost"
                    size="sm"
                    className="shrink-0 text-xs text-text-muted"
                    aria-expanded={expanded}
                    onClick={() => setExpanded((prev) => !prev)}
                >
                    {expanded ? (
                        <>
                            Show less <ChevronUpIcon className="ml-1 size-3.5" />
                        </>
                    ) : (
                        <>
                            Show more <ChevronDownIcon className="ml-1 size-3.5" />
                        </>
                    )}
                </Button>
            </div>

            {expanded && (
                <p className="mt-4 border-t border-border pt-4 text-sm text-text-muted">{course.description}</p>
            )}
        </div>
    );
}
