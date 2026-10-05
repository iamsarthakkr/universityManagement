import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { formatIsoDateRange } from '@/lib/date';
import { formatSemesterName } from '@/lib/semester';
import { Semester, SemesterStatus } from '@/types/semester';
import { SemesterActions } from './SemesterActions';
import { SemesterStatusBadge } from './SemesterStatusBadge';

type Props = {
    semesters: Semester[];
    pendingId: number | null;
    onTransition: (semester: Semester, status: SemesterStatus) => void;
};

export function SemestersTable({ semesters, pendingId, onTransition }: Props) {
    const hasActions = semesters.some((semester) => semester.allowedTransitions.length > 0);

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
                        <TableCell className="text-center font-semibold">{formatSemesterName(semester)}</TableCell>
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
                                {semester.allowedTransitions.length > 0 && (
                                    <SemesterActions
                                        semester={semester}
                                        disabled={pendingId === semester.id}
                                        onTransition={onTransition}
                                    />
                                )}
                            </TableCell>
                        )}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
