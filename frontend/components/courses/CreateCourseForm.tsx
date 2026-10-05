import { useMutation, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/base/button';
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { DepartmentSelect } from '@/components/common/DepartmentSelect';
import { PageHeader } from '@/components/common/PageHeader';
import { useFormState } from '@/hooks/useFormState';
import { normalizeCodeSuffix } from '@/lib/courseCode';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { useApi } from '@/stores/apiStore';
import { useAppStore } from '@/stores/appStore';
import { CourseRequest } from '@/types/course';
import { Department } from '@/types/department';

import { FormLayout, FormSection } from '@/components/common/FormLayout';

type CourseFormData = Omit<CourseRequest, 'departmentId' | 'code'>;

const initialFormData: CourseFormData = {
    title: '',
    description: '',
    credits: 1,
};

export function CreateCourseForm() {
    const api = useApi();
    const departments = useAppStore((state) => state.staticData.departments);

    const { values, handleChange, reset } = useFormState(initialFormData);
    const [selectedDept, setSelectedDept] = React.useState<Department | null>(null);
    const [codeInput, setCodeInput] = React.useState('');

    const codeSuffix = selectedDept ? normalizeCodeSuffix(codeInput, selectedDept.code) : '';
    const courseCode = selectedDept && codeSuffix ? `${selectedDept.code}${codeSuffix}` : null;

    const handleDepartmentChange = React.useCallback(
        (departmentId: number) => {
            setSelectedDept(departments.find((department) => department.id === departmentId) ?? null);
        },
        [departments],
    );

    const handleCodeChange = React.useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setCodeInput(event.target.value.toUpperCase());
    }, []);

    const queryClient = useQueryClient();

    const createCourse = useMutation({
        mutationFn: (request: CourseRequest) => unwrap(api.courses.createCourse(request)),
        onSuccess: () => {
            toast.success('Course created successfully.');
            reset();
            setSelectedDept(null);
            setCodeInput('');
            return queryClient.invalidateQueries({ queryKey: queryKeys.courses.all });
        },
        onError: (error) => {
            toast.error('Failed to create course', { description: error.message });
        },
    });

    const handleSubmit = (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!selectedDept || !courseCode) {
            toast.error('Enter a course number after the department code.');
            return;
        }

        createCourse.mutate({ ...values, departmentId: selectedDept.id, code: courseCode });
    };

    return (
        <>
            <PageHeader title="Create new course" description="Add a new course to the university catalog." />
            <FormLayout>
                <form onSubmit={handleSubmit}>
                    <div className="flex flex-col gap-8">
                        <FormSection label="Identity" description="How this course is identified in the catalog.">
                            <FieldGroup>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field>
                                        <FieldLabel htmlFor="department">Department</FieldLabel>
                                        <DepartmentSelect
                                            id="department"
                                            required
                                            value={selectedDept?.id ?? null}
                                            onChange={handleDepartmentChange}
                                        />
                                    </Field>
                                    <Field>
                                        <FieldLabel htmlFor="code">Course Code</FieldLabel>
                                        <div className="flex items-center gap-1">
                                            {selectedDept && (
                                                <span className="flex h-8 items-center rounded-lg border border-input bg-muted px-2.5 text-sm font-medium text-muted-foreground select-none">
                                                    {selectedDept.code}
                                                </span>
                                            )}
                                            <Input
                                                id="code"
                                                name="code"
                                                type="text"
                                                required
                                                disabled={!selectedDept}
                                                placeholder={selectedDept ? 'e.g. 101' : 'Select a department first'}
                                                value={codeInput}
                                                onChange={handleCodeChange}
                                            />
                                        </div>
                                        {courseCode && <FieldDescription>Saved as {courseCode}</FieldDescription>}
                                    </Field>
                                </div>
                                <Field>
                                    <FieldLabel htmlFor="title">Title</FieldLabel>
                                    <Input
                                        id="title"
                                        name="title"
                                        type="text"
                                        required
                                        placeholder="e.g. Introduction to Programming"
                                        value={values.title}
                                        onChange={handleChange}
                                    />
                                </Field>
                                <Field>
                                    <FieldLabel htmlFor="description">Description</FieldLabel>
                                    <Input
                                        id="description"
                                        name="description"
                                        type="text"
                                        required
                                        placeholder="e.g. Covers fundamentals of programming using Python"
                                        value={values.description}
                                        onChange={handleChange}
                                    />
                                </Field>
                            </FieldGroup>
                        </FormSection>

                        <FormSection label="Credits" description="Set the academic weight of this course.">
                            <FieldGroup>
                                <Field>
                                    <FieldLabel htmlFor="credits">Credits (1–10)</FieldLabel>
                                    <Input
                                        id="credits"
                                        name="credits"
                                        type="number"
                                        min={1}
                                        max={10}
                                        required
                                        placeholder="e.g. 3"
                                        value={values.credits}
                                        onChange={handleChange}
                                    />
                                </Field>
                            </FieldGroup>
                        </FormSection>

                        <div className="flex justify-end">
                            <Button type="submit" disabled={createCourse.isPending} className="min-w-36">
                                {createCourse.isPending ? 'Creating...' : 'Create course'}
                            </Button>
                        </div>
                    </div>
                </form>
            </FormLayout>
        </>
    );
}
