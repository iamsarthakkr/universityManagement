import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { Callback1 } from '@/types/common';
import { RegistrationKind, RegistrationResponse, RegistrationStatus } from '@/types/registration';
import { AdminActions, Status } from './common';

interface Props {
    kind: RegistrationKind;
    items: RegistrationResponse[];
    pendingId: number | null;
    onApprove: Callback1<number>;
    onReject: Callback1<number>;
}

export const RegistrationsTable = (props: Props) => {
    const { kind, items, pendingId, onApprove, onReject } = props;
    const showDateOfBirth = kind === 'student';
    const hasActions = items.some((item) => item.status === RegistrationStatus.PENDING);

    return (
        <Table>
            <TableHeader className="bg-surface-muted">
                <TableRow>
                    <TableHead className="text-center">Name</TableHead>
                    <TableHead className="text-center">Username</TableHead>
                    <TableHead className="text-center">Email</TableHead>
                    {showDateOfBirth && <TableHead className="text-center">Date of Birth</TableHead>}
                    <TableHead className="text-center">Department</TableHead>
                    <TableHead className="text-center">Status</TableHead>
                    <TableHead className="text-center">Submitted</TableHead>
                    {hasActions && <TableHead className="text-center">Actions</TableHead>}
                </TableRow>
            </TableHeader>

            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell className="font-semibold text-center">
                            {item.firstName + (item.lastName ? ' ' + item.lastName : '')}
                        </TableCell>
                        <TableCell className="text-center">{item.username}</TableCell>
                        <TableCell className="text-center">{item.email}</TableCell>
                        {showDateOfBirth && (
                            <TableCell className="text-center">
                                {'dateOfBirth' in item ? item.dateOfBirth : ''}
                            </TableCell>
                        )}
                        <TableCell className="text-center">{item.department.name}</TableCell>
                        <TableCell className="text-center">
                            <Status status={item.status} />
                        </TableCell>
                        <TableCell className="text-center">{item.submittedAt}</TableCell>

                        {hasActions && (
                            <TableCell className="text-center">
                                {item.status === RegistrationStatus.PENDING && (
                                    <AdminActions
                                        id={item.id}
                                        disabled={pendingId === item.id}
                                        onApprove={onApprove}
                                        onReject={onReject}
                                    />
                                )}
                            </TableCell>
                        )}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};
