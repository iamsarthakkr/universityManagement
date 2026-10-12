import { Link } from 'react-router';

import { ActionMenu } from '@/components/common/ActionMenu';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { formatIsoDateRange } from '@/lib/date';
import { formatSemesterName } from '@/lib/semester';
import { Semester, SemesterAction } from '@/types/semester';
import { SEMESTER_ACTIONS } from './semesterActions';
import { SemesterStatusBadge } from './SemesterStatusBadge';

type Props = {
    semesters: Semester[];
    pendingIds: number[];
    onAction: (semester: Semester, action: SemesterAction) => void;
};

export function SemestersTable({ semesters, pendingIds, onAction }: Props) {
    const hasActions = semesters.some((semester) => semester.allowedActions.length > 0);

    return (
        <Table>
            <TableHeader className="bg-surface-muted">
                <TableRow>
                    <TableHead className="text-center">Semester</TableHead>
                    <TableHead className="text-center">Status</TableHead>
                    <TableHead className="text-center">Registration</TableHead>
                    <TableHead className="text-center">Teaching period</TableHead>
                    {hasActions && <TableHead className="text-center">Actions</TableHead>}
                </TableRow>
            </TableHeader>

            <TableBody>
                {semesters.map((semester) => (
                    <TableRow key={semester.id}>
                        <TableCell className="text-center font-semibold">
                            <Link
                                to={`/dashboard/admin/semesters/${semester.id}`}
                                className="text-brand underline-offset-4 hover:underline"
                            >
                                {formatSemesterName(semester)}
                            </Link>
                        </TableCell>
                        <TableCell className="text-center">
                            <SemesterStatusBadge status={semester.status} />
                        </TableCell>
                        <TableCell className="text-center">
                            {formatIsoDateRange(semester.registrationStartDate, semester.registrationEndDate)}
                        </TableCell>
                        <TableCell className="text-center">
                            {formatIsoDateRange(semester.startDate, semester.endDate)}
                        </TableCell>
                        {hasActions && (
                            <TableCell className="text-center">
                                <ActionMenu
                                    actions={semester.allowedActions}
                                    config={SEMESTER_ACTIONS}
                                    subject={formatSemesterName(semester)}
                                    disabled={pendingIds.includes(semester.id)}
                                    onAction={(action) => onAction(semester, action)}
                                />
                            </TableCell>
                        )}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
