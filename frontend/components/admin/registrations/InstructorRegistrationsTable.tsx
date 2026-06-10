'use client';

import React from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { RegistrationStatus, InstructorRegistrationResponse } from '@/types/registration';
import { Callback1 } from '@/types/common';
import { AdminActions, Status } from './common';

interface Props {
    items: InstructorRegistrationResponse[];
    onApprove: Callback1<number>;
    onReject: Callback1<number>;
}

export const InstructorRegistrationsTable = (props: Props) => {
    const { items, onApprove, onReject } = props;

    return (
        <Table>
            <TableHeader className="bg-surface-muted">
                <TableRow>
                    <TableHead className="text-center">Name</TableHead>
                    <TableHead className="text-center">Username</TableHead>
                    <TableHead className="text-center">Email</TableHead>
                    <TableHead className="text-center">Department</TableHead>
                    <TableHead className="text-center">Status</TableHead>
                    <TableHead className="text-center">Submitted</TableHead>
                    <TableHead className="text-center">Actions</TableHead>
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
                        <TableCell className="text-center">{item.department}</TableCell>
                        <TableCell className="text-center">
                            <Status status={item.status} />
                        </TableCell>
                        <TableCell className="text-center">{item.submittedAt}</TableCell>

                        <TableCell className="text-center">
                            <AdminActions
                                id={item.id}
                                showActions={item.status === RegistrationStatus.PENDING}
                                onApprove={onApprove}
                                onReject={onReject}
                            />
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};
