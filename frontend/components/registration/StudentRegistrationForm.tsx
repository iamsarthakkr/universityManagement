'use client';

import React from 'react';
import Link from 'next/link';

import { cn } from '@/lib/cn';
import { Button } from '@/components/ui/base/button';
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../ui/base/card';
import { useApi } from '@/context/ApiContext';
import type { StudentRegistrationRequest } from '@/types/registration';

const initialFormData: StudentRegistrationRequest = {
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
};

export const StudentRegistrationForm = ({ className, ...props }: React.ComponentProps<'div'>) => {
    const api = useApi();

    const [formData, setFormData] = React.useState<StudentRegistrationRequest>(initialFormData);

    const [isSubmitting, setIsSubmitting] = React.useState(false);
    const [message, setMessage] = React.useState<string | null>(null);

    const handleChange = React.useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = event.target;

        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    }, []);

    const handleSubmit = React.useCallback(
        async (event: React.SubmitEvent<HTMLFormElement>) => {
            event.preventDefault();

            setIsSubmitting(true);
            setMessage(null);

            const res = await api.registration.createStudentRegistration(formData);

            setIsSubmitting(false);

            if (!res.isSuccess) {
                setMessage(res.message || 'Unable to submit request.');
                return;
            }

            setMessage(res.message || 'Registration request submitted.');
            setFormData(initialFormData);
        },
        [api, formData],
    );

    return (
        <div className={cn('flex flex-col gap-6', className)} {...props}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Student registration</CardTitle>

                    <CardDescription>Submit student registration request for admin approval.</CardDescription>
                </CardHeader>

                <CardContent>
                    <form onSubmit={handleSubmit}>
                        <FieldGroup>
                            <Field>
                                <FieldLabel htmlFor="username">Username</FieldLabel>

                                <Input
                                    id="username"
                                    name="username"
                                    type="text"
                                    required
                                    value={formData.username}
                                    onChange={handleChange}
                                />
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="password">Password</FieldLabel>

                                <Input
                                    id="password"
                                    name="password"
                                    type="password"
                                    required
                                    value={formData.password}
                                    onChange={handleChange}
                                />
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="email">Email</FieldLabel>

                                <Input
                                    id="email"
                                    name="email"
                                    type="email"
                                    required
                                    value={formData.email}
                                    onChange={handleChange}
                                />
                            </Field>

                            <div className="grid gap-4 md:grid-cols-2">
                                <Field>
                                    <FieldLabel htmlFor="firstName">Firstname</FieldLabel>

                                    <Input
                                        id="firstName"
                                        name="firstName"
                                        type="text"
                                        required
                                        value={formData.firstName}
                                        onChange={handleChange}
                                    />
                                </Field>

                                <Field>
                                    <FieldLabel htmlFor="lastName">Lastname</FieldLabel>

                                    <Input
                                        id="lastName"
                                        name="lastName"
                                        type="text"
                                        value={formData.lastName}
                                        onChange={handleChange}
                                    />
                                </Field>
                            </div>

                            <Field>
                                <FieldLabel htmlFor="dateOfBirth">Date of Birth</FieldLabel>

                                <Input
                                    id="dateOfBirth"
                                    name="dateOfBirth"
                                    type="date"
                                    required
                                    value={formData.dateOfBirth}
                                    onChange={handleChange}
                                />
                            </Field>

                            {message && (
                                <p className="rounded-xl bg-surface-muted px-4 py-3 text-sm text-text-muted">
                                    {message}
                                </p>
                            )}

                            <Field>
                                <Button type="submit" className="mt-2 w-full" disabled={isSubmitting}>
                                    {isSubmitting ? 'Submitting...' : 'Submit request'}
                                </Button>
                            </Field>
                        </FieldGroup>
                    </form>
                </CardContent>

                <CardFooter>
                    <FieldDescription className="mx-auto px-6 text-center">
                        <p className="text-center text-sm text-text-muted">
                            Already registered?{' '}
                            <Link className="font-semibold text-brand" href="/login">
                                Login
                            </Link>
                        </p>
                    </FieldDescription>
                </CardFooter>
            </Card>
        </div>
    );
};
