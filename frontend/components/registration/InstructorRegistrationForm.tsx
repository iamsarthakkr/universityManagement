'use client';

import React from 'react';
import Link from 'next/link';

import { cn } from '@/lib/cn';
import { Button } from '@/components/ui/base/button';
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../ui/base/card';
import { useApi } from '@/context/ApiContext';
import { useStaticData } from '@/context/StaticDataContext';
import type { InstructorRegistrationData } from '@/types/registration';
import { toast } from 'sonner';
import RegistrationLayout from './RegistrationLayout';

const initialFormData: InstructorRegistrationData = {
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    department: '',
};

export const InstructorRegistrationForm = ({ className, ...props }: React.ComponentProps<'div'>) => {
    const api = useApi();
    const { departments, isLoading: depsLoading } = useStaticData();

    const [formData, setFormData] = React.useState<InstructorRegistrationData>(initialFormData);

    const [isSubmitting, setIsSubmitting] = React.useState(false);

    const handleChange = React.useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = event.target;

        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    }, []);

    const handleSubmit = React.useCallback(
        async (event: React.SubmitEvent<HTMLFormElement>) => {
            setIsSubmitting(true);

            event.preventDefault();
            const res = await api.registration.createInstructorRegistration(formData);

            if (!res.isSuccess) {
                toast.error('Request failed', { description: res.message || 'Unable to submit request.' });
                return;
            }

            toast.error(res.message);
            setFormData(initialFormData);

            setIsSubmitting(false);
        },
        [api, formData],
    );

    return (
        <RegistrationLayout
            title="Instructor registration"
            description="Submit instructor registration request for approval."
        >
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
                        <FieldLabel htmlFor="department">Department</FieldLabel>
                        <select
                            id="department"
                            name="department"
                            required
                            disabled={depsLoading}
                            value={formData.department}
                            onChange={(e) => setFormData((prev) => ({ ...prev, department: e.target.value }))}
                            className={cn(
                                'h-8 w-full min-w-0 rounded-lg border border-input bg-transparent px-2.5 py-1 text-base transition-colors outline-none',
                                'focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50',
                                'disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-input/50 disabled:opacity-50',
                                'md:text-sm dark:bg-input/30',
                            )}
                        >
                            <option value="" disabled>
                                {depsLoading ? 'Loading...' : 'Select a department'}
                            </option>
                            {departments.map((d) => (
                                <option key={d.id} value={d.name}>
                                    {d.name}
                                </option>
                            ))}
                        </select>
                    </Field>

                    <Field>
                        <Button type="submit" className="mt-2 w-full" disabled={isSubmitting}>
                            {isSubmitting ? 'Submitting...' : 'Submit request'}
                        </Button>
                    </Field>
                </FieldGroup>
            </form>
        </RegistrationLayout>
    );
};
