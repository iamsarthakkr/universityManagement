import { useMutation } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { DepartmentSelect } from '@/components/common/DepartmentSelect';
import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { useFormState } from '@/hooks/useFormState';
import { unwrap } from '@/lib/query';
import { toLocalIsoDate } from '@/lib/date';
import { useApi } from '@/stores/apiStore';
import type { StudentRegistrationRequest } from '@/types/registration';
import { AccountFields } from './AccountFields';
import RegistrationLayout from './RegistrationLayout';

const initialValues: StudentRegistrationRequest = {
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    departmentId: 0,
};

export const StudentRegistrationForm = () => {
    const api = useApi();
    const { values, handleChange, setField, reset } = useFormState(initialValues);
    const submitRegistration = useMutation({
        mutationFn: (request: StudentRegistrationRequest) =>
            unwrap(api.registration.createStudentRegistration(request)),
        onSuccess: () => {
            toast.success('Registration request submitted for approval.');
            reset();
        },
        onError: (error) => {
            toast.error('Request failed', { description: error.message });
        },
    });

    const handleSubmit = (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        submitRegistration.mutate(values);
    };

    return (
        <RegistrationLayout
            title="Student registration"
            description="Submit student registration request for approval."
        >
            <form onSubmit={handleSubmit}>
                <FieldGroup>
                    <AccountFields values={values} onChange={handleChange} />

                    <Field>
                        <FieldLabel htmlFor="dateOfBirth">Date of birth</FieldLabel>
                        <Input
                            id="dateOfBirth"
                            name="dateOfBirth"
                            type="date"
                            autoComplete="bday"
                            required
                            max={toLocalIsoDate(new Date())}
                            value={values.dateOfBirth}
                            onChange={handleChange}
                        />
                    </Field>

                    <Field>
                        <FieldLabel htmlFor="department">Department</FieldLabel>
                        <DepartmentSelect
                            id="department"
                            required
                            value={values.departmentId || null}
                            onChange={(departmentId) => setField('departmentId', departmentId)}
                        />
                    </Field>

                    <Field>
                        <Button type="submit" className="mt-2 w-full" disabled={submitRegistration.isPending}>
                            {submitRegistration.isPending ? 'Submitting...' : 'Submit request'}
                        </Button>
                    </Field>
                </FieldGroup>
            </form>
        </RegistrationLayout>
    );
};
