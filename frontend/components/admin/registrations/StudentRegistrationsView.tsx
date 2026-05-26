'use client';

import { MoreHorizontalIcon } from 'lucide-react';

import { PageHeader } from '@/components/common/PageHeader';
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
import { RegistrationStatus, StudentRegistrationResponse } from '@/types/registration';
import React from 'react';
import { useApi } from '@/context/ApiContext';

type RegistrationViewProps = {
    title: string;
    description: string;
    placeholder?: string;
    status: RegistrationStatus;
};

const statusStyles: Record<RegistrationStatus, string> = {
    PENDING: 'bg-yellow-50 text-yellow-700 ring-yellow-600/20',
    APPROVED: 'bg-green-50 text-green-700 ring-green-600/20',
    REJECTED: 'bg-red-50 text-red-700 ring-red-600/20',
};

export const StudentRegistrationsView = ({
    title,
    description,
    placeholder = 'No registration requests found.',
    status,
}: RegistrationViewProps) => {
    const api = useApi();
    const [items, setItems] = React.useState<StudentRegistrationResponse[]>([]);
    const [isLoading, setIsLoading] = React.useState(true);
    const [error, setError] = React.useState<string | null>(null);

    React.useEffect(() => {
        const asyncFetch = async () => {
            setIsLoading(true);

            const res = await api.admin.getPendingStudentRegistrations();
            if (!res.isSuccess || !res.body) {
                setError(res.message);
                return;
            }
            setItems(res.body);

            setIsLoading(false);
        };
        asyncFetch();
    }, []);

    if (isLoading) return <p>Loading...</p>;

    if (error) return <p>{error}</p>;

    return (
        <div className="space-y-6">
            <PageHeader title={title} description={description} />

            <div className="overflow-hidden rounded-3xl border border-border bg-white shadow-soft">
                {items.length === 0 ? (
                    <div className="p-6">
                        <p className="text-sm text-muted-foreground">No pending student registration requests</p>
                    </div>
                ) : (
                    <Table>
                        <TableHeader className="bg-surface-muted">
                            <TableRow>
                                <TableHead className="text-center">Name</TableHead>
                                <TableHead className="text-center">Username</TableHead>
                                <TableHead className="text-center">Email</TableHead>
                                <TableHead className="text-center">Date of Birth</TableHead>
                                <TableHead className="text-center">Status</TableHead>
                                <TableHead className="text-center">Submitted</TableHead>
                                <TableHead className="text-center">Actions</TableHead>
                            </TableRow>
                        </TableHeader>

                        <TableBody>
                            {items.map((item) => (
                                <TableRow key={item.id}>
                                    <TableCell className="font-semibold text-center">
                                        {item.firstName + ' ' + item.lastName}
                                    </TableCell>

                                    <TableCell className="text-center">{item.username}</TableCell>

                                    <TableCell className="text-center">{item.email}</TableCell>

                                    <TableCell className="text-center">{item.dateOfBirth}</TableCell>

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

                                                        <DropdownMenuItem>Approve</DropdownMenuItem>

                                                        <DropdownMenuItem variant="destructive">
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
                )}
            </div>
        </div>
    );
};
