import { SemesterStatusBadge } from '@/components/semesters/SemesterStatusBadge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { fullName } from '@/lib/format';
import { formatSemesterName } from '@/lib/semester';
import { CourseOffering } from '@/types/offering';
import { OfferingSeats } from './OfferingSeats';

type Props = {
    offerings: CourseOffering[];
    showSemester?: boolean;
};

export function OfferingsTable({ offerings, showSemester = true }: Props) {
    return (
        <Table>
            <TableHeader className="bg-surface-muted">
                <TableRow>
                    <TableHead>Course</TableHead>
                    <TableHead className="text-center">Section</TableHead>
                    {showSemester && <TableHead className="text-center">Semester</TableHead>}
                    <TableHead className="text-center">Instructor</TableHead>
                    <TableHead>Seats</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {offerings.map((offering) => (
                    <TableRow key={offering.id}>
                        <TableCell>
                            <div className="font-semibold text-text">{offering.course.code}</div>
                            <div className="text-xs text-text-muted">{offering.course.title}</div>
                        </TableCell>
                        <TableCell className="text-center">{offering.section}</TableCell>
                        {showSemester && (
                            <TableCell className="text-center">
                                <div className="flex flex-col items-center gap-1">
                                    <span>{formatSemesterName(offering.semester)}</span>
                                    <SemesterStatusBadge status={offering.semester.status} />
                                </div>
                            </TableCell>
                        )}
                        <TableCell className="text-center">{fullName(offering.instructor)}</TableCell>
                        <TableCell>
                            <OfferingSeats
                                enrolled={offering.enrolled}
                                capacity={offering.capacity}
                                seatsAvailable={offering.seatsAvailable}
                            />
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
