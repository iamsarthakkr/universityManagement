'use client';

import React from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { useApi } from '@/context/ApiContext';
import { useStaticData } from '@/context/StaticDataContext';
import { CourseRequest } from '@/types/course';
import { Department } from '@/types/department';

import { PageHeader } from '@/components/common/PageHeader';
import { cn } from '@/lib/cn';

import { CourseFormLayout, CourseFormSection } from './CourseFormLayout';

const initialFormData: CourseRequest = {
    department: '',
    code: '',
    title: '',
    description: '',
    credits: 1,
    capacity: 1,
    instructorId: 0,
};

export function CreateCourseForm() {
    const api = useApi();
    const { departments, isLoading: depsLoading } = useStaticData();

    const [formData, setFormData] = React.useState<CourseRequest>(initialFormData);
    const [selectedDept, setSelectedDept] = React.useState<Department | null>(null);
    const [codeWithoutPrefix, setCodeWithoutPrefix] = React.useState('');
    const [isSubmitting, setIsSubmitting] = React.useState(false);

    const handleDepartmentChange = React.useCallback(
        (event: React.ChangeEvent<HTMLSelectElement>) => {
            const dept = departments.find((d) => d.id === Number(event.target.value)) ?? null;
            setSelectedDept(dept);
            setFormData((prev) => ({
                ...prev,
                department: dept?.name ?? '',
                code: dept ? `${dept.code}${codeWithoutPrefix}` : codeWithoutPrefix,
            }));
        },
        [departments, codeWithoutPrefix],
    );

    const handleCodeSuffixChange = React.useCallback(
        (event: React.ChangeEvent<HTMLInputElement>) => {
            const suffix = event.target.value;
            setCodeWithoutPrefix(suffix);
            setFormData((prev) => ({
                ...prev,
                code: selectedDept ? `${selectedDept.code}${suffix}` : suffix,
            }));
        },
        [selectedDept],
    );

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
            setIsSubmitting(true);

            const res = await api.courses.createCourse(formData);

            setIsSubmitting(false);

            if (!res.isSuccess) {
                toast.error('Failed to create course', { description: res.message || 'Unable to submit request.' });
                return;
            }

            toast.success(res.message || 'Course created successfully.');
            setFormData(initialFormData);
            setSelectedDept(null);
            setCodeWithoutPrefix('');
        },
        [api, formData],
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
                                            disabled={depsLoading}
                                            value={selectedDept?.id ?? ''}
                                            onChange={handleDepartmentChange}
                                            className={selectClassName}
                                        >
                                            <option value="" disabled>
                                                {depsLoading ? 'Loading...' : 'Select a department'}
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
                                                placeholder={selectedDept ? '101' : 'e.g. CS101'}
                                                value={codeWithoutPrefix}
                                                onChange={handleCodeSuffixChange}
                                            />
                                        </div>
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

                        <CourseFormSection
                            label="Capacity & Credits"
                            description="Set enrollment limits and academic weight."
                        >
                            <FieldGroup>
                                <div className="grid gap-4 sm:grid-cols-2">
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
                                    <Field>
                                        <FieldLabel htmlFor="capacity">Capacity</FieldLabel>
                                        <Input
                                            id="capacity"
                                            name="capacity"
                                            type="number"
                                            min={1}
                                            required
                                            placeholder="e.g. 30"
                                            value={formData.capacity}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                </div>
                            </FieldGroup>
                        </CourseFormSection>

                        <CourseFormSection label="Instructor" description="Assign an instructor to this course.">
                            <FieldGroup>
                                <Field>
                                    <FieldLabel htmlFor="instructorId">Instructor ID</FieldLabel>
                                    <Input
                                        id="instructorId"
                                        name="instructorId"
                                        type="number"
                                        min={1}
                                        required
                                        placeholder="e.g. 42"
                                        value={formData.instructorId || ''}
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
