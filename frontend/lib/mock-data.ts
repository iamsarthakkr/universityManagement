import { RegistrationStatus } from '@/types/registration';

export const pendingStudentRegistrations = [
    {
        id: 1,
        firstName: 'Aarav',
        lastName: 'Mehta',
        username: 'aarav.m',
        email: 'aarav@example.com',
        submittedAt: 'Today',
        status: RegistrationStatus.PENDING,
    },
    {
        id: 2,
        firstName: 'Nisha',
        lastName: 'Rao',
        username: 'nisha.r',
        email: 'nisha@example.com',
        submittedAt: 'Yesterday',
        status: RegistrationStatus.PENDING,
    },
];

export const pendingInstructorRegistrations = [
    {
        id: 1,
        firstName: 'Dr. Kavya',
        lastName: 'Sen',
        username: 'kavya.s',
        email: 'kavya@example.com',
        submittedAt: 'Today',
        status: RegistrationStatus.PENDING,
    },
    {
        id: 2,
        firstName: 'Prof. Rohan',
        lastName: 'Iyer',
        username: 'rohan.i',
        email: 'rohan@example.com',
        submittedAt: '2 days ago',
        status: RegistrationStatus.PENDING,
    },
];

export const dashboardStats = [
    { label: 'Pending Students', value: '24' },
    { label: 'Pending Instructors', value: '8' },
    { label: 'Active Courses', value: '42' },
    { label: 'Total Enrollments', value: '1,284' },
];
