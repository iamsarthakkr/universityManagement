import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import React from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { toast } from 'sonner';

import { DepartmentSelect } from '@/components/common/DepartmentSelect';
import { FormLayout, FormSection } from '@/components/common/FormLayout';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/ui/base/button';
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { NativeSelect } from '@/components/ui/base/nativeSelect';
import { useFormState } from '@/hooks/useFormState';
import { idParam } from '@/hooks/useFilterParams';
import { fullName } from '@/lib/format';
import { unwrap } from '@/lib/query';
import { queryKeys } from '@/lib/queryKeys';
import { formatSemesterName } from '@/lib/semester';
import { useApi } from '@/stores/apiStore';
import { CreateCourseOfferingRequest } from '@/types/offering';
import { Semester } from '@/types/semester';

type FormValues = {
    semesterId: number;
    departmentId: number;
    courseId: number;
    instructorId: number;
    section: string;
    capacity: number;
};

function acceptingNewestFirst(semesters: Semester[]) {
    return semesters
        .filter((semester) => semester.acceptsOfferings)
        .sort((a, b) => b.startDate.localeCompare(a.startDate));
}

function dependentPlaceholder(query: { isPending: boolean; isError: boolean }, ready: boolean, noun: string) {
    if (!ready) return 'Select a department first';
    if (query.isError) return `Couldn't load ${noun}s`;
    if (query.isPending) return `Loading ${noun}s...`;
    return `Select a ${noun}`;
}

export function CreateOfferingForm() {
    const api = useApi();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [searchParams] = useSearchParams();

    const { values, handleChange, setField } = useFormState<FormValues>({
        semesterId: idParam(searchParams.get('semesterId') ?? '') ?? 0,
        departmentId: 0,
        courseId: 0,
        instructorId: 0,
        section: '',
        capacity: 30,
    });
    const hasDepartment = values.departmentId > 0;

    const semestersQuery = useQuery({
        queryKey: queryKeys.semesters.list,
        queryFn: () => unwrap(api.semesters.getSemesters()),
        select: acceptingNewestFirst,
    });
    const semesters = semestersQuery.data ?? [];
    // A preselected semester that can't take offerings is ignored rather than shown as a stale choice.
    const semesterId = semesters.some((semester) => semester.id === values.semesterId) ? values.semesterId : 0;

    const departmentFilter = { departmentId: values.departmentId };
    const coursesQuery = useQuery({
        queryKey: queryKeys.courses.list(departmentFilter),
        queryFn: () => unwrap(api.courses.getCourses(departmentFilter)),
        enabled: hasDepartment,
    });
    const instructorsQuery = useQuery({
        queryKey: queryKeys.instructors.list(departmentFilter),
        queryFn: () => unwrap(api.instructors.getInstructors(departmentFilter)),
        enabled: hasDepartment,
    });

    const handleDepartmentChange = (departmentId: number) => {
        setField('departmentId', departmentId);
        setField('courseId', 0);
        setField('instructorId', 0);
    };

    const createOffering = useMutation({
        mutationFn: (request: CreateCourseOfferingRequest) => unwrap(api.offerings.createOffering(request)),
        onSuccess: async (offering) => {
            toast.success(`${offering.course.code} section ${offering.section} added.`);
            await queryClient.invalidateQueries({ queryKey: queryKeys.offerings.all });
            navigate(`/dashboard/admin/semesters/${offering.semester.id}`);
        },
        onError: (error) => {
            toast.error('Failed to create offering', { description: error.message });
        },
    });

    const handleSubmit = (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        createOffering.mutate({
            semesterId,
            courseId: values.courseId,
            instructorId: values.instructorId,
            section: values.section.trim().toUpperCase(),
            capacity: values.capacity,
        });
    };

    const noSemesterAccepts = semestersQuery.isSuccess && semesters.length === 0;

    return (
        <>
            <PageHeader
                title="Create offering"
                description="Schedule a course in a semester with an instructor and seats."
            />
            <FormLayout>
                <form onSubmit={handleSubmit}>
                    <div className="flex flex-col gap-8">
                        <FormSection
                            label="Semester"
                            description="Only semesters that accept new offerings are listed."
                        >
                            <FieldGroup>
                                <Field>
                                    <FieldLabel htmlFor="semester">Semester</FieldLabel>
                                    <NativeSelect
                                        id="semester"
                                        required
                                        disabled={!semestersQuery.isSuccess}
                                        value={semesterId || ''}
                                        onChange={(event) => setField('semesterId', Number(event.target.value))}
                                    >
                                        <option value="" disabled>
                                            {semestersQuery.isError ? "Couldn't load semesters" : 'Select a semester'}
                                        </option>
                                        {semesters.map((semester) => (
                                            <option key={semester.id} value={semester.id}>
                                                {formatSemesterName(semester)}
                                            </option>
                                        ))}
                                    </NativeSelect>
                                    {semestersQuery.isError && (
                                        <FieldDescription>{semestersQuery.error.message}</FieldDescription>
                                    )}
                                    {noSemesterAccepts && (
                                        <FieldDescription>
                                            No semester is accepting offerings right now.{' '}
                                            <Link to="/dashboard/admin/semesters/new" className="text-brand">
                                                Create a semester
                                            </Link>
                                        </FieldDescription>
                                    )}
                                </Field>
                            </FieldGroup>
                        </FormSection>

                        <FormSection
                            label="Course and instructor"
                            description="The instructor must belong to the course's department."
                        >
                            <FieldGroup>
                                <Field>
                                    <FieldLabel htmlFor="department">Department</FieldLabel>
                                    <DepartmentSelect
                                        id="department"
                                        required
                                        value={values.departmentId || null}
                                        onChange={handleDepartmentChange}
                                    />
                                </Field>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field>
                                        <FieldLabel htmlFor="course">Course</FieldLabel>
                                        <NativeSelect
                                            id="course"
                                            required
                                            disabled={!coursesQuery.isSuccess}
                                            value={values.courseId || ''}
                                            onChange={(event) => setField('courseId', Number(event.target.value))}
                                        >
                                            <option value="" disabled>
                                                {dependentPlaceholder(coursesQuery, hasDepartment, 'course')}
                                            </option>
                                            {(coursesQuery.data ?? []).map((course) => (
                                                <option key={course.id} value={course.id}>
                                                    {course.code} · {course.title}
                                                </option>
                                            ))}
                                        </NativeSelect>
                                        {coursesQuery.isSuccess && coursesQuery.data.length === 0 && (
                                            <FieldDescription>This department has no courses yet.</FieldDescription>
                                        )}
                                    </Field>
                                    <Field>
                                        <FieldLabel htmlFor="instructor">Instructor</FieldLabel>
                                        <NativeSelect
                                            id="instructor"
                                            required
                                            disabled={!instructorsQuery.isSuccess}
                                            value={values.instructorId || ''}
                                            onChange={(event) => setField('instructorId', Number(event.target.value))}
                                        >
                                            <option value="" disabled>
                                                {dependentPlaceholder(instructorsQuery, hasDepartment, 'instructor')}
                                            </option>
                                            {(instructorsQuery.data ?? []).map((instructor) => (
                                                <option key={instructor.id} value={instructor.id}>
                                                    {fullName(instructor)}
                                                </option>
                                            ))}
                                        </NativeSelect>
                                        {instructorsQuery.isSuccess && instructorsQuery.data.length === 0 && (
                                            <FieldDescription>This department has no instructors yet.</FieldDescription>
                                        )}
                                    </Field>
                                </div>
                            </FieldGroup>
                        </FormSection>

                        <FormSection
                            label="Section and seats"
                            description="Sections tell apart several offerings of one course."
                        >
                            <FieldGroup>
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field>
                                        <FieldLabel htmlFor="section">Section</FieldLabel>
                                        <Input
                                            id="section"
                                            name="section"
                                            type="text"
                                            required
                                            maxLength={10}
                                            placeholder="e.g. A"
                                            value={values.section}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                    <Field>
                                        <FieldLabel htmlFor="capacity">Capacity</FieldLabel>
                                        <Input
                                            id="capacity"
                                            name="capacity"
                                            type="number"
                                            required
                                            min={1}
                                            value={values.capacity}
                                            onChange={handleChange}
                                        />
                                    </Field>
                                </div>
                            </FieldGroup>
                        </FormSection>

                        <div className="flex justify-end">
                            <Button type="submit" disabled={createOffering.isPending} className="min-w-36">
                                {createOffering.isPending ? 'Creating...' : 'Create offering'}
                            </Button>
                        </div>
                    </div>
                </form>
            </FormLayout>
        </>
    );
}
