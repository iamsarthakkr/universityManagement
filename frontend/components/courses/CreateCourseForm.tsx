'use client';

import React from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { useApi } from '@/context/ApiContext';
import { CourseRequest } from '@/types/course';

import { PageHeader } from '@/components/common/PageHeader';

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

    const [formData, setFormData] = React.useState<CourseRequest>(initialFormData);
    const [isSubmitting, setIsSubmitting] = React.useState(false);

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
        },
        [api, formData],
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
                                    <Input
                                        id="department"
                                        name="department"
                                        type="text"
                                        required
                                        placeholder="e.g. Computer Science"
                                        value={formData.department}
                                        onChange={handleChange}
                                    />
                                </Field>
                                <Field>
                                    <FieldLabel htmlFor="code">Course Code</FieldLabel>
                                    <Input
                                        id="code"
                                        name="code"
                                        type="text"
                                        required
                                        placeholder="e.g. CS101"
                                        value={formData.code}
                                        onChange={handleChange}
                                    />
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
