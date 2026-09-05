'use client';

import React from 'react';
import { ChevronDownIcon, ChevronUpIcon } from 'lucide-react';

import { Button } from '@/components/ui/base/button';
import { CourseResponse } from '@/types/course';

export function CourseCard({ course }: { course: CourseResponse }) {
    const [expanded, setExpanded] = React.useState(false);

    return (
        <div className="rounded-2xl border border-border bg-surface-muted p-4">
            <div className="flex items-start justify-between gap-4">
                <div>
                    <div className="flex items-center gap-2">
                        <span className="text-xs font-semibold uppercase tracking-wide text-text-muted">
                            {course.code}
                        </span>
                    </div>
                    <h4 className="mt-0.5 font-semibold text-text">{course.title}</h4>
                </div>
                <Button
                    variant="ghost"
                    size="sm"
                    className="shrink-0 text-xs text-text-muted"
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
                <div className="mt-4 space-y-3 border-t border-border pt-4">
                    <p className="text-sm text-text-muted">{course.description}</p>
                    <div className="flex flex-wrap gap-4 text-sm">
                        <span>
                            <span className="font-medium text-text">Credits:</span>{' '}
                            <span className="text-text-muted">{course.credits}</span>
                        </span>
                        <span>
                            <span className="font-medium text-text">Capacity:</span>{' '}
                            <span className="text-text-muted">{course.capacity}</span>
                        </span>
                        <span>
                            <span className="font-medium text-text">Instructor:</span>{' '}
                            <span className="text-text-muted">{course.instructor}</span>
                        </span>
                    </div>
                </div>
            )}
        </div>
    );
}
