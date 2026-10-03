import { useMutation } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { DepartmentSelect } from '@/components/common/DepartmentSelect';
import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { useFormState } from '@/hooks/useFormState';
import { unwrap } from '@/lib/query';
import { useApi } from '@/stores/apiStore';
import type { InstructorRegistrationRequest } from '@/types/registration';
import { AccountFields } from './AccountFields';
import RegistrationLayout from './RegistrationLayout';

const initialValues: InstructorRegistrationRequest = {
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    departmentId: 0,
};

export const InstructorRegistrationForm = () => {
    const api = useApi();
    const { values, handleChange, setField, reset } = useFormState(initialValues);
    const submitRegistration = useMutation({
        mutationFn: (request: InstructorRegistrationRequest) =>
            unwrap(api.registration.createInstructorRegistration(request)),
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
            title="Instructor registration"
            description="Submit instructor registration request for approval."
        >
            <form onSubmit={handleSubmit}>
                <FieldGroup>
                    <AccountFields values={values} onChange={handleChange} />

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
