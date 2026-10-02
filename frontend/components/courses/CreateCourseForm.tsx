import React from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/base/button';
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { useApi } from '@/stores/apiStore';
import { useAppStore } from '@/stores/appStore';
import { CourseRequest } from '@/types/course';
import { Department } from '@/types/department';

import { PageHeader } from '@/components/common/PageHeader';
import { cn } from '@/lib/cn';

import { CourseFormLayout, CourseFormSection } from './CourseFormLayout';

type CourseFormData = Omit<CourseRequest, 'departmentId' | 'code'>;

const initialFormData: CourseFormData = {
    title: '',
    description: '',
    credits: 1,
};

function normalizeCodeSuffix(input: string, departmentCode: string) {
    const code = input.replace(/\s+/g, '').toUpperCase();
    return code.startsWith(departmentCode) ? code.slice(departmentCode.length) : code;
}

export function CreateCourseForm() {
    const api = useApi();
    const departments = useAppStore((state) => state.staticData.departments);

    const [formData, setFormData] = React.useState<CourseFormData>(initialFormData);
    const [selectedDept, setSelectedDept] = React.useState<Department | null>(null);
    const [codeInput, setCodeInput] = React.useState('');
    const [isSubmitting, setIsSubmitting] = React.useState(false);

    const codeSuffix = selectedDept ? normalizeCodeSuffix(codeInput, selectedDept.code) : '';
    const courseCode = selectedDept && codeSuffix ? `${selectedDept.code}${codeSuffix}` : null;

    const handleDepartmentChange = React.useCallback(
        (event: React.ChangeEvent<HTMLSelectElement>) => {
            setSelectedDept(departments.find((d) => d.id === Number(event.target.value)) ?? null);
        },
        [departments],
    );

    const handleCodeChange = React.useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setCodeInput(event.target.value.toUpperCase());
    }, []);

    const handleChange = React.useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type } = event.target;
        setFormData((prev) => ({
            ...prev,
            [name]: type === 'number' ? Number(value) : value,
        }));
    }, []);

    const handleSubmit = React.useCallback(
        async (event: React.SubmitEvent<HTMLFormElement>) => {
            event.preventDefault();

            if (!selectedDept || !courseCode) {
                toast.error('Enter a course number after the department code.');
                return;
            }

            setIsSubmitting(true);

            const res = await api.courses.createCourse({
                ...formData,
                departmentId: selectedDept.id,
                code: courseCode,
            });

            setIsSubmitting(false);

            if (!res.isSuccess) {
                toast.error('Failed to create course', { description: res.message || 'Unable to submit request.' });
                return;
            }

            toast.success(res.message || 'Course created successfully.');
            setFormData(initialFormData);
            setSelectedDept(null);
            setCodeInput('');
        },
        [api, formData, selectedDept, courseCode],
    );

    const selectClassName = cn(
        'h-8 w-full min-w-0 rounded-lg border border-input bg-transparent px-2.5 py-1 text-base transition-colors outline-none',
        'focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50',
        'disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-input/50 disabled:opacity-50',
        'md:text-sm dark:bg-input/30',
    );

    return (
        <>
            <PageHeader title="Create new course" description="Add a new course to the university catalog." />
            <CourseFormLayout>
                <form onSubmit={handleSubmit}>
                    <div className="flex flex-col gap-8">
                        <CourseFormSection label="Identity" description="How this course is identified in the catalog.">
                            <FieldGroup>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field>
                                        <FieldLabel htmlFor="department">Department</FieldLabel>
                                        <select
                                            id="department"
                                            required
                                            value={selectedDept?.id ?? ''}
                                            onChange={handleDepartmentChange}
                                            className={selectClassName}
                                        >
                                            <option value="" disabled>
                                                Select a department
                                            </option>
                                            {departments.map((d) => (
                                                <option key={d.id} value={d.id}>
                                                    {d.name}
                                                </option>
                                            ))}
                                        </select>
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
                                        value={formData.title}
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
                                        value={formData.description}
                                        onChange={handleChange}
                                    />
                                </Field>
                            </FieldGroup>
                        </CourseFormSection>

                        <CourseFormSection label="Credits" description="Set the academic weight of this course.">
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
                                        value={formData.credits}
                                        onChange={handleChange}
                                    />
                                </Field>
                            </FieldGroup>
                        </CourseFormSection>

                        <div className="flex justify-end">
                            <Button type="submit" disabled={isSubmitting} className="min-w-36">
                                {isSubmitting ? 'Creating...' : 'Create course'}
                            </Button>
                        </div>
                    </div>
                </form>
            </CourseFormLayout>
        </>
    );
}
