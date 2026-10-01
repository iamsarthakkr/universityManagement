import { createBrowserRouter, Navigate } from 'react-router';

import { DashboardHomeRedirect, RoleGuard } from '@/components/auth/RoleGuard';
import RootLayout from '@/pages/RootLayout';

import AuthLayout from '@/pages/auth/AuthLayout';
import LoginPage from '@/pages/auth/login/page';
import StudentRegistrationPage from '@/pages/auth/registration/student/page';
import InstructorRegistrationPage from '@/pages/auth/registration/instructor/page';

import DashboardLayout from '@/pages/dashboard/DashboardLayout';
import AdminDashboardPage from '@/pages/dashboard/admin/page';
import PendingStudentRegistrationsPage from '@/pages/dashboard/admin/student-registrations/pending/page';
import ApprovedStudentRegistrationsPage from '@/pages/dashboard/admin/student-registrations/approved/page';
import RejectedStudentRegistrationsPage from '@/pages/dashboard/admin/student-registrations/rejected/page';
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
                    { index: true, element: <DashboardHomeRedirect /> },
                    {
                        path: 'admin',
                        element: <RoleGuard roles={['ADMIN']} />,
                        children: [
                            { index: true, element: <AdminDashboardPage /> },
                            { path: 'student-registrations/pending', element: <PendingStudentRegistrationsPage /> },
                            { path: 'student-registrations/approved', element: <ApprovedStudentRegistrationsPage /> },
                            { path: 'student-registrations/rejected', element: <RejectedStudentRegistrationsPage /> },
                            {
                                path: 'instructor-registrations/pending',
                                element: <PendingInstructorRegistrationsPage />,
                            },
                            {
                                path: 'instructor-registrations/approved',
                                element: <ApprovedInstructorRegistrationsPage />,
                            },
                            {
                                path: 'instructor-registrations/rejected',
                                element: <RejectedInstructorRegistrationsPage />,
                            },
                        ],
                    },
                    {
                        path: 'student',
                        element: <RoleGuard roles={['STUDENT']} />,
                        children: [
                            { index: true, element: <StudentHomePage /> },
                            { path: 'courses', element: <StudentCoursesPage /> },
                            { path: 'enrollments', element: <StudentEnrollmentsPage /> },
                        ],
                    },
                    {
                        path: 'instructor',
                        element: <RoleGuard roles={['INSTRUCTOR']} />,
                        children: [
                            { index: true, element: <InstructorHomePage /> },
                            { path: 'courses', element: <InstructorCoursesPage /> },
                        ],
                    },
                    { path: 'courses', element: <CoursesPage /> },
                    {
                        path: 'courses/new',
                        element: <RoleGuard roles={['ADMIN']} />,
                        children: [{ index: true, element: <NewCoursePage /> }],
                    },
                ],
            },
            { path: '*', element: <Navigate to="/" replace /> },
        ],
    },
]);
