import { Me, Role } from '@/types/auth';
import { Course } from '@/types/course';
import { Department } from '@/types/department';
import { Enrollment, EnrollmentStatus } from '@/types/enrollment';
import { Instructor } from '@/types/instructor';
import { CourseOffering } from '@/types/offering';
import {
    InstructorRegistrationResponse,
    RegistrationAction,
    RegistrationStatus,
    StudentRegistrationResponse,
} from '@/types/registration';
import { Semester, SemesterAction, SemesterStatus, SemesterTerm } from '@/types/semester';
import { Student } from '@/types/student';

export const CS: Department = { id: 1, name: 'Computer Science', code: 'CS' };
export const PHYSICS: Department = { id: 2, name: 'Physics', code: 'PH' };

export function me(role: Role, overrides: Partial<Me> = {}): Me {
    return {
        id: 1,
        username: 'sam',
        email: 'sam@uni.edu',
        role,
        profile: role === 'ADMIN' ? null : { id: 11, firstName: 'Sam', lastName: 'Rao', department: CS },
        ...overrides,
    };
}

export function semester(overrides: Partial<Semester> = {}): Semester {
    return {
        id: 1,
        term: SemesterTerm.WINTER,
        year: 2026,
        status: SemesterStatus.PLANNED,
        registrationStartDate: '2026-10-01',
        registrationEndDate: '2026-10-15',
        startDate: '2026-11-01',
        endDate: '2027-03-31',
        allowedActions: [SemesterAction.ACTIVATE, SemesterAction.CANCEL],
        isRegistrationOpen: false,
        acceptsOfferings: true,
        ...overrides,
    };
}

export function course(overrides: Partial<Course> = {}): Course {
    return {
        id: 10,
        department: CS,
        code: 'CS101',
        title: 'Algorithms',
        description: 'Sorting',
        credits: 4,
        ...overrides,
    };
}

export function instructor(overrides: Partial<Instructor> = {}): Instructor {
    return { id: 4, firstName: 'Kavya', lastName: 'Sen', email: 'kavya@uni.edu', department: CS, ...overrides };
}

export function student(overrides: Partial<Student> = {}): Student {
    return { id: 7, firstName: 'Ana', lastName: 'Gomez', email: 'ana@uni.edu', department: CS, ...overrides };
}

export function offering(overrides: Partial<CourseOffering> = {}): CourseOffering {
    const { id, term, year, status, isRegistrationOpen } = semester();
    return {
        id: 9,
        course: { id: 10, code: 'CS101', title: 'Algorithms', credits: 4 },
        instructor: { id: 4, firstName: 'Kavya', lastName: 'Sen' },
        semester: { id, term, year, status, isRegistrationOpen },
        section: 'A',
        capacity: 30,
        enrolled: 12,
        seatsAvailable: 18,
        allowedActions: [],
        ...overrides,
    };
}

export function enrollment(overrides: Partial<Enrollment> = {}): Enrollment {
    const { id, course, instructor, semester, section } = offering();
    return {
        id: 5,
        status: EnrollmentStatus.PENDING,
        student: { id: 7, firstName: 'Ana', lastName: 'Gomez' },
        courseOffering: { id, course, instructor, semester, section },
        allowedActions: [],
        createdAt: '2026-10-02T09:30:00Z',
        updatedAt: '2026-10-02T09:30:00Z',
        ...overrides,
    };
}

function registrationBase(id: number, status: RegistrationStatus): InstructorRegistrationResponse {
    const pending = status === RegistrationStatus.PENDING;
    return {
        id,
        username: `user${id}`,
        email: `user${id}@uni.edu`,
        firstName: 'Student',
        lastName: String(id),
        status,
        createdAt: '2026-10-01T08:00:00Z',
        reviewedAt: pending ? null : '2026-10-03T08:00:00Z',
        department: CS,
        allowedActions: pending ? [RegistrationAction.APPROVE, RegistrationAction.REJECT] : [],
    };
}

export function studentRegistration(
    id: number,
    status = RegistrationStatus.PENDING,
    overrides: Partial<StudentRegistrationResponse> = {},
): StudentRegistrationResponse {
    return {
        ...registrationBase(id, status),
        username: `student${id}`,
        dateOfBirth: '2004-05-06',
        ...overrides,
    };
}

export function instructorRegistration(
    id: number,
    status = RegistrationStatus.PENDING,
    overrides: Partial<InstructorRegistrationResponse> = {},
): InstructorRegistrationResponse {
    return { ...registrationBase(id, status), username: `instructor${id}`, ...overrides };
}
