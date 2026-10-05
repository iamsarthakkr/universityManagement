import { useMutation, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { useNavigate } from 'react-router';
import { toast } from 'sonner';

import { FormLayout, FormSection } from '@/components/common/FormLayout';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { NativeSelect } from '@/components/ui/base/nativeSelect';
import { useFormState } from '@/hooks/useFormState';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { formatSemesterName } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { CreateSemesterRequest, SemesterTerm } from '@/types/semester';

const initialFormData: CreateSemesterRequest = {
    term: SemesterTerm.WINTER,
    year: new Date().getFullYear(),
    registrationStartDate: '',
    registrationEndDate: '',
    startDate: '',
    endDate: '',
};

export function CreateSemesterForm() {
    const api = useApi();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { values, handleChange, setField } = useFormState(initialFormData);

    const createSemester = useMutation({
        mutationFn: (request: CreateSemesterRequest) => unwrap(api.semesters.createSemester(request)),
        onSuccess: async (semester) => {
            toast.success(`${formatSemesterName(semester)} created.`);
            await queryClient.invalidateQueries({ queryKey: queryKeys.semesters.all });
            navigate('/dashboard/admin/semesters');
        },
        onError: (error) => {
            toast.error('Failed to create semester', { description: error.message });
        },
    });

    const handleSubmit = (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        createSemester.mutate(values);
    };

    return (
        <>
            <PageHeader title="Create semester" description="New semesters start as planned." />
            <FormLayout>
                <form onSubmit={handleSubmit}>
                    <div className="flex flex-col gap-8">
                        <FormSection label="Term" description="Which part of which academic year this is.">
                            <FieldGroup>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field>
                                        <FieldLabel htmlFor="term">Term</FieldLabel>
                                        <NativeSelect
                                            id="term"
                                            required
                                            value={values.term}
                                            onChange={(event) => setField('term', event.target.value as SemesterTerm)}
                                        >
                                            <option value={SemesterTerm.WINTER}>Winter</option>
                                            <option value={SemesterTerm.SUMMER}>Summer</option>
                                        </NativeSelect>
                                    </Field>
                                    <Field>
                                        <FieldLabel htmlFor="year">Year</FieldLabel>
                                        <Input
                                            id="year"
                                            name="year"
                                            type="number"
                                            required
                                            value={values.year}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                </div>
                            </FieldGroup>
                        </FormSection>

                        <FormSection label="Registration window" description="When students can request enrollment.">
                            <FieldGroup>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field>
                                        <FieldLabel htmlFor="registrationStartDate">Opens</FieldLabel>
                                        <Input
                                            id="registrationStartDate"
                                            name="registrationStartDate"
                                            type="date"
                                            required
                                            value={values.registrationStartDate}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                    <Field>
                                        <FieldLabel htmlFor="registrationEndDate">Closes</FieldLabel>
                                        <Input
                                            id="registrationEndDate"
                                            name="registrationEndDate"
                                            type="date"
                                            required
                                            value={values.registrationEndDate}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                </div>
                            </FieldGroup>
                        </FormSection>

                        <FormSection label="Teaching period" description="When classes run.">
                            <FieldGroup>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field>
                                        <FieldLabel htmlFor="startDate">Starts</FieldLabel>
                                        <Input
                                            id="startDate"
                                            name="startDate"
                                            type="date"
                                            required
                                            value={values.startDate}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                    <Field>
                                        <FieldLabel htmlFor="endDate">Ends</FieldLabel>
                                        <Input
                                            id="endDate"
                                            name="endDate"
                                            type="date"
                                            required
                                            value={values.endDate}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                </div>
                            </FieldGroup>
                        </FormSection>

                        <div className="flex justify-end">
                            <Button type="submit" disabled={createSemester.isPending} className="min-w-36">
                                {createSemester.isPending ? 'Creating...' : 'Create semester'}
                            </Button>
                        </div>
                    </div>
                </form>
            </FormLayout>
        </>
    );
}
