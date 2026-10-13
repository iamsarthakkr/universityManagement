import { ActionMenu } from '@/components/common/ActionMenu';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { formatDateTime, formatIsoDate } from '@/lib/date';
import { fullName } from '@/lib/format';
import { RegistrationAction, RegistrationKind, RegistrationResponse } from '@/types/registration';
import { REGISTRATION_ACTIONS, Status } from './common';

interface Props {
    kind: RegistrationKind;
    items: RegistrationResponse[];
    pendingIds: number[];
    onAction: (id: number, action: RegistrationAction) => void;
}

export const RegistrationsTable = (props: Props) => {
    const { kind, items, pendingIds, onAction } = props;
    const showDateOfBirth = kind === 'student';
    const showReviewed = items.some((item) => item.reviewedAt !== null);
    const hasActions = items.some((item) => item.allowedActions.length > 0);

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
                    {showReviewed && <TableHead className="text-center">Reviewed</TableHead>}
                    {hasActions && <TableHead className="text-center">Actions</TableHead>}
                </TableRow>
            </TableHeader>
            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell className="font-semibold text-center">{fullName(item)}</TableCell>
                        <TableCell className="text-center">{item.username}</TableCell>
                        <TableCell className="text-center">{item.email}</TableCell>
                        {showDateOfBirth && (
                            <TableCell className="text-center">
                                {'dateOfBirth' in item ? formatIsoDate(item.dateOfBirth) : ''}
                            </TableCell>
                        )}
                        <TableCell className="text-center">{item.department.name}</TableCell>
                        <TableCell className="text-center">
                            <Status status={item.status} />
                        </TableCell>
                        <TableCell className="text-center">{formatDateTime(item.createdAt)}</TableCell>
                        {showReviewed && (
                            <TableCell className="text-center">
                                {item.reviewedAt ? formatDateTime(item.reviewedAt) : ''}
                            </TableCell>
                        )}
                        {hasActions && (
                            <TableCell className="text-center">
                                <ActionMenu
                                    actions={item.allowedActions}
                                    config={REGISTRATION_ACTIONS}
                                    subject={item.username}
                                    disabled={pendingIds.includes(item.id)}
                                    onAction={(action) => onAction(item.id, action)}
                                />
                            </TableCell>
                        )}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};
