import { ActionMenu } from '@/components/common/ActionMenu';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { formatDateTime } from '@/lib/date';
import { fullName } from '@/lib/format';
import { Enrollment, EnrollmentAction } from '@/types/enrollment';
import { ENROLLMENT_ACTIONS } from './enrollmentActions';
import { EnrollmentStatusBadge } from './EnrollmentStatusBadge';

type Props = {
    enrollments: Enrollment[];
    pendingIds: number[];
    onAction: (enrollmentId: number, action: EnrollmentAction) => void;
};

export function RosterTable({ enrollments, pendingIds, onAction }: Props) {
    const hasActions = enrollments.some((enrollment) => enrollment.allowedActions.length > 0);

    return (
        <Table>
            <TableHeader className="bg-surface-muted">
                <TableRow>
                    <TableHead>Student</TableHead>
                    <TableHead className="text-center">Status</TableHead>
                    <TableHead className="text-center">Requested</TableHead>
                    <TableHead className="text-center">Last change</TableHead>
                    {hasActions && <TableHead className="text-center">Actions</TableHead>}
                </TableRow>
            </TableHeader>
            <TableBody>
                {enrollments.map((enrollment) => {
                    const student = fullName(enrollment.student);
                    return (
                        <TableRow key={enrollment.id}>
                            <TableCell className="font-semibold">{student}</TableCell>
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
                                        subject={student}
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
