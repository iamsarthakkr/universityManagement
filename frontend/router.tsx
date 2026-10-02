import { ComponentType } from 'react';
import { createBrowserRouter, Navigate } from 'react-router';

import { DashboardHomeRedirect, RoleGuard } from '@/components/auth/RoleGuard';
import { LoadingOverlay } from '@/components/common/AppStateOverlay';
import AuthLayout from '@/pages/auth/AuthLayout';
import DashboardLayout from '@/pages/dashboard/DashboardLayout';
import RootLayout from '@/pages/RootLayout';

function lazyPage(load: () => Promise<{ default: ComponentType }>) {
    return async () => {
        const module = await load();
        return { Component: module.default };
    };
}

export const router = createBrowserRouter([
    {
        element: <RootLayout />,
        HydrateFallback: LoadingOverlay,
        children: [
            { index: true, element: <Navigate to="/login" replace /> },
            {
                element: <AuthLayout />,
                children: [
                    { path: 'login', lazy: lazyPage(() => import('@/pages/auth/login/page')) },
                    {
                        path: 'registration/student',
                        lazy: lazyPage(() => import('@/pages/auth/registration/student/page')),
                    },
                    {
                        path: 'registration/instructor',
                        lazy: lazyPage(() => import('@/pages/auth/registration/instructor/page')),
                    },
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
                            { index: true, lazy: lazyPage(() => import('@/pages/dashboard/admin/page')) },
                            {
                                path: 'student-registrations/pending',
                                lazy: lazyPage(
                                    () => import('@/pages/dashboard/admin/student-registrations/pending/page'),
                                ),
                            },
                            {
                                path: 'student-registrations/approved',
                                lazy: lazyPage(
                                    () => import('@/pages/dashboard/admin/student-registrations/approved/page'),
                                ),
                            },
                            {
                                path: 'student-registrations/rejected',
                                lazy: lazyPage(
                                    () => import('@/pages/dashboard/admin/student-registrations/rejected/page'),
                                ),
                            },
                            {
                                path: 'instructor-registrations/pending',
                                lazy: lazyPage(
                                    () => import('@/pages/dashboard/admin/instructor-registrations/pending/page'),
                                ),
                            },
                            {
                                path: 'instructor-registrations/approved',
                                lazy: lazyPage(
                                    () => import('@/pages/dashboard/admin/instructor-registrations/approved/page'),
                                ),
                            },
                            {
                                path: 'instructor-registrations/rejected',
                                lazy: lazyPage(
                                    () => import('@/pages/dashboard/admin/instructor-registrations/rejected/page'),
                                ),
                            },
                        ],
                    },
                    {
                        path: 'student',
                        element: <RoleGuard roles={['STUDENT']} />,
                        children: [
                            { index: true, lazy: lazyPage(() => import('@/pages/dashboard/student/page')) },
                            { path: 'courses', lazy: lazyPage(() => import('@/pages/dashboard/student/courses/page')) },
                            {
                                path: 'enrollments',
                                lazy: lazyPage(() => import('@/pages/dashboard/student/enrollments/page')),
                            },
                        ],
                    },
                    {
                        path: 'instructor',
                        element: <RoleGuard roles={['INSTRUCTOR']} />,
                        children: [
                            { index: true, lazy: lazyPage(() => import('@/pages/dashboard/instructor/page')) },
                            {
                                path: 'courses',
                                lazy: lazyPage(() => import('@/pages/dashboard/instructor/courses/page')),
                            },
                        ],
                    },
                    { path: 'courses', lazy: lazyPage(() => import('@/pages/dashboard/courses/page')) },
                    {
                        path: 'courses/new',
                        element: <RoleGuard roles={['ADMIN']} />,
                        children: [{ index: true, lazy: lazyPage(() => import('@/pages/dashboard/courses/new/page')) }],
                    },
                ],
            },
            { path: '*', element: <Navigate to="/" replace /> },
        ],
    },
]);
