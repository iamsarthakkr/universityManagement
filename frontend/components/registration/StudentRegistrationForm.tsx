'use client';

import React from 'react';

import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { useApi } from '@/context/ApiContext';
import type { StudentRegistrationRequest } from '@/types/registration';
import { toast } from 'sonner';
import RegistrationLayout from './RegistrationLayout';

const initialFormData: StudentRegistrationRequest = {
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
};

export const StudentRegistrationForm = () => {
    const api = useApi();

    const [formData, setFormData] = React.useState<StudentRegistrationRequest>(initialFormData);

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

            const res = await api.registration.createStudentRegistration(formData);

            setIsSubmitting(false);

            if (!res.isSuccess) {
                toast.error('Request failed', { description: res.message || 'Unable to submit request.' });
                return;
            }

            toast.error(res.message);

            setFormData(initialFormData);
        },
        [api, formData],
    );

    return (
        <RegistrationLayout
            title="Student Registration"
            description="Submit student registration request for approval."
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
