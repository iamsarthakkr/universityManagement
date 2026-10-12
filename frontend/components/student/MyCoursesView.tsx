import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router';

import { PageHeader } from '@/components/common/PageHeader';
import { QueryState } from '@/components/common/QueryState';
import { SemesterSections } from '@/components/semesters/SemesterSections';
import { Button } from '@/components/ui/base/button';
import { formatCredits, fullName } from '@/lib/format';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { groupBySemester } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { Enrollment, EnrollmentStatus } from '@/types/enrollment';

const ENROLLED = { status: EnrollmentStatus.ENROLLED };

function CourseCard({ enrollment }: { enrollment: Enrollment }) {
    const { course, section, instructor } = enrollment.courseOffering;
    return (
        <article
            aria-label={course.code}
            className="flex w-full flex-col gap-1 rounded-2xl border border-border bg-surface p-5 shadow-soft"
        >
            <p className="text-xs font-semibold uppercase tracking-wide text-text-muted">
                {course.code} · Section {section}
            </p>
            <h3 className="font-semibold text-text">{course.title}</h3>
            <p className="text-sm text-text-muted">
                {fullName(instructor)} · {formatCredits(course.credits)}
            </p>
        </article>
    );
}

export function MyCoursesView() {
    const api = useApi();
    const enrollmentsQuery = useQuery({
        queryKey: queryKeys.enrollments.mine(ENROLLED),
        queryFn: () => unwrap(api.enrollments.getMyEnrollments(ENROLLED)),
    });
    const enrollments = enrollmentsQuery.data ?? [];
    // Already newest semester first from the server.
    const groups = groupBySemester(enrollments, (enrollment) => enrollment.courseOffering.semester);

    return (
        <div className="space-y-6">
            <PageHeader title="My courses" description="Courses you are enrolled in, by semester." />
            {enrollmentsQuery.isSuccess && enrollments.length === 0 ? (
                <div className="flex flex-col items-center gap-3 rounded-3xl border border-dashed border-border p-10 text-center">
                    <p className="text-sm text-text-muted">You are not enrolled in any course yet.</p>
                    <Button asChild size="sm">
                        <Link to="/dashboard/student/offerings">Browse offerings</Link>
                    </Button>
                </div>
            ) : (
                <QueryState query={enrollmentsQuery} isEmpty={false} emptyMessage="">
                    <SemesterSections
                        groups={groups}
                        itemKey={(enrollment) => enrollment.id}
                        renderItem={(enrollment) => <CourseCard enrollment={enrollment} />}
                        renderMeta={(group) => (
                            <span className="text-sm text-text-muted">
                                {formatCredits(
                                    group.items.reduce((total, item) => total + item.courseOffering.course.credits, 0),
                                )}
                            </span>
                        )}
                    />
                </QueryState>
            )}
        </div>
    );
}
