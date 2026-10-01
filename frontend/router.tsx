import { createBrowserRouter, Navigate } from 'react-router';

import RootLayout from '@/pages/RootLayout';

import AuthLayout from '@/pages/auth/AuthLayout';
import LoginPage from '@/pages/auth/login/page';
import StudentRegistrationPage from '@/pages/auth/registration/student/page';
import InstructorRegistrationPage from '@/pages/auth/registration/instructor/page';

import DashboardLayout from '@/pages/dashboard/DashboardLayout';
import AdminDashboardPage from '@/pages/dashboard/admin/page';
import StudentRegistrationsPage from '@/pages/dashboard/admin/student-registrations/page';
import PendingStudentRegistrationsPage from '@/pages/dashboard/admin/student-registrations/pending/page';
import ApprovedStudentRegistrationsPage from '@/pages/dashboard/admin/student-registrations/approved/page';
import RejectedStudentRegistrationsPage from '@/pages/dashboard/admin/student-registrations/rejected/page';
import InstructorRegistrationsPage from '@/pages/dashboard/admin/instructor-registrations/page';
import PendingInstructorRegistrationsPage from '@/pages/dashboard/admin/instructor-registrations/pending/page';
import ApprovedInstructorRegistrationsPage from '@/pages/dashboard/admin/instructor-registrations/approved/page';
import RejectedInstructorRegistrationsPage from '@/pages/dashboard/admin/instructor-registrations/rejected/page';
import CoursesPage from '@/pages/dashboard/courses/page';
import NewCoursePage from '@/pages/dashboard/courses/new/page';
import StudentHomePage from '@/pages/dashboard/student/page';
import StudentCoursesPage from '@/pages/dashboard/student/courses/page';
import StudentEnrollmentsPage from '@/pages/dashboard/student/enrollments/page';
import InstructorHomePage from '@/pages/dashboard/instructor/page';
import InstructorCoursesPage from '@/pages/dashboard/instructor/courses/page';

export const router = createBrowserRouter([
    {
        element: <RootLayout />,
        children: [
            { index: true, element: <Navigate to="/login" replace /> },
            {
                element: <AuthLayout />,
                children: [
                    { path: 'login', element: <LoginPage /> },
                    { path: 'registration/student', element: <StudentRegistrationPage /> },
                    { path: 'registration/instructor', element: <InstructorRegistrationPage /> },
                ],
            },
            {
                path: 'dashboard',
                element: <DashboardLayout />,
                children: [
                    { path: 'admin', element: <AdminDashboardPage /> },
                    { path: 'admin/student-registrations', element: <StudentRegistrationsPage /> },
                    { path: 'admin/student-registrations/pending', element: <PendingStudentRegistrationsPage /> },
                    { path: 'admin/student-registrations/approved', element: <ApprovedStudentRegistrationsPage /> },
                    { path: 'admin/student-registrations/rejected', element: <RejectedStudentRegistrationsPage /> },
                    { path: 'admin/instructor-registrations', element: <InstructorRegistrationsPage /> },
                    { path: 'admin/instructor-registrations/pending', element: <PendingInstructorRegistrationsPage /> },
                    { path: 'admin/instructor-registrations/approved', element: <ApprovedInstructorRegistrationsPage /> },
                    { path: 'admin/instructor-registrations/rejected', element: <RejectedInstructorRegistrationsPage /> },
                    { path: 'courses', element: <CoursesPage /> },
                    { path: 'courses/new', element: <NewCoursePage /> },
                    { path: 'student', element: <StudentHomePage /> },
                    { path: 'student/courses', element: <StudentCoursesPage /> },
                    { path: 'student/enrollments', element: <StudentEnrollmentsPage /> },
                    { path: 'instructor', element: <InstructorHomePage /> },
                    { path: 'instructor/courses', element: <InstructorCoursesPage /> },
                ],
            },
            // Next.js rendered a built-in 404 here; send unknown URLs back to the entry point instead.
            { path: '*', element: <Navigate to="/" replace /> },
        ],
    },
]);
