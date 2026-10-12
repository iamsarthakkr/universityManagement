import { ActionMenu } from '@/components/common/ActionMenu';
import { ENROLLMENT_ACTIONS } from '@/components/enrollments/enrollmentActions';
import { EnrollmentStatusBadge } from '@/components/enrollments/EnrollmentStatusBadge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { formatDateTime } from '@/lib/date';
import { fullName } from '@/lib/format';
import { formatSemesterName } from '@/lib/semester';
import { Enrollment, EnrollmentAction } from '@/types/enrollment';

type Props = {
    enrollments: Enrollment[];
    pendingIds: number[];
    onAction: (enrollmentId: number, action: EnrollmentAction) => void;
};

export function EnrollmentsTable({ enrollments, pendingIds, onAction }: Props) {
    const hasActions = enrollments.some((enrollment) => enrollment.allowedActions.length > 0);

    return (
        <Table>
            <TableHeader className="bg-surface-muted">
                <TableRow>
                    <TableHead>Course</TableHead>
                    <TableHead className="text-center">Semester</TableHead>
                    <TableHead className="text-center">Section</TableHead>
                    <TableHead className="text-center">Instructor</TableHead>
                    <TableHead className="text-center">Status</TableHead>
                    <TableHead className="text-center">Requested</TableHead>
                    <TableHead className="text-center">Last change</TableHead>
                    {hasActions && <TableHead className="text-center">Actions</TableHead>}
                </TableRow>
            </TableHeader>
            <TableBody>
                {enrollments.map((enrollment) => {
                    const { course, semester, section, instructor } = enrollment.courseOffering;
                    return (
                        <TableRow key={enrollment.id}>
                            <TableCell>
                                <div className="font-semibold text-text">{course.code}</div>
                                <div className="text-xs text-text-muted">{course.title}</div>
                            </TableCell>
                            <TableCell className="text-center">{formatSemesterName(semester)}</TableCell>
                            <TableCell className="text-center">{section}</TableCell>
                            <TableCell className="text-center">{fullName(instructor)}</TableCell>
                            <TableCell className="text-center">
                                <EnrollmentStatusBadge status={enrollment.status} />
                            </TableCell>
                            <TableCell className="text-center">{formatDateTime(enrollment.createdAt)}</TableCell>
                            <TableCell className="text-center">{formatDateTime(enrollment.updatedAt)}</TableCell>
                            {hasActions && (
                                <TableCell className="text-center">
                                    <ActionMenu
                                        actions={enrollment.allowedActions}
                                        config={ENROLLMENT_ACTIONS}
                                        subject={course.code}
                                        disabled={pendingIds.includes(enrollment.id)}
                                        onAction={(action) => onAction(enrollment.id, action)}
                                    />
                                </TableCell>
                            )}
                        </TableRow>
                    );
                })}
            </TableBody>
        </Table>
    );
}
