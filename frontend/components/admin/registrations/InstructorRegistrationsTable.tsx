'use client';

import React from 'react';
import { MoreHorizontalIcon } from 'lucide-react';

import { Button } from '@/components/ui/base/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/base/dropdownMenu';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/base/table';
import { cn } from '@/lib/cn';
import { RegistrationStatus, InstructorRegistrationResponse } from '@/types/registration';
import { Callback1 } from '@/types/common';

const statusStyles: Record<RegistrationStatus, string> = {
    PENDING: 'bg-yellow-50 text-yellow-700 ring-yellow-600/20',
    APPROVED: 'bg-green-50 text-green-700 ring-green-600/20',
    REJECTED: 'bg-red-50 text-red-700 ring-red-600/20',
};

interface Props {
    items: InstructorRegistrationResponse[];
    onApprove?: Callback1<InstructorRegistrationResponse>;
    onReject?: Callback1<InstructorRegistrationResponse>;
}
export const InstructorRegistrationsTable = (props: Props) => {
    const { items, onApprove, onReject } = props;

    const handleApprove = React.useCallback(
        (item: InstructorRegistrationResponse) => {
            if (onApprove) {
                onApprove(item);
            }
        },
        [onApprove],
    );

    const handleReject = React.useCallback(
        (item: InstructorRegistrationResponse) => {
            if (onReject) {
                onReject(item);
            }
        },
        [onReject],
    );

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
                            <span
                                className={cn(
                                    'inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset',
                                    statusStyles[item.status],
                                )}
                            >
                                {item.status}
                            </span>
                        </TableCell>
                        <TableCell className="text-center">{item.submittedAt}</TableCell>

                        <TableCell className="text-center">
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" size="icon" className="size-8">
                                        <MoreHorizontalIcon className="size-4" />
                                        <span className="sr-only">Open menu</span>
                                    </Button>
                                </DropdownMenuTrigger>

                                <DropdownMenuContent align="end">
                                    <DropdownMenuItem>View details</DropdownMenuItem>

                                    {item.status === 'PENDING' && (
                                        <>
                                            <DropdownMenuSeparator />
                                            <DropdownMenuItem onClick={() => handleApprove(item)}>
                                                Approve
                                            </DropdownMenuItem>
                                            <DropdownMenuItem onClick={() => handleReject(item)} variant="destructive">
                                                Reject
                                            </DropdownMenuItem>
                                        </>
                                    )}
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};
