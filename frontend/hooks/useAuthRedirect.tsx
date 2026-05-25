'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

import { useAuth } from '@/context/AuthContext';
import { Role } from '@/types/auth';

type UseAuthRedirectOptions = {
    requireAuth?: boolean;
    allowedRoles?: Array<Role>;
    redirectAuthenticatedTo?: string;
};

function getDashboardPathByRole(role: Role) {
    switch (role) {
        case 'ADMIN':
            return '/dashboard/admin';

        case 'STUDENT':
            return '/dashboard/student';

        case 'INSTRUCTOR':
            return '/dashboard/instructor';
    }
}

export function useAuthRedirect(options: UseAuthRedirectOptions = {}) {
    const { requireAuth = false, allowedRoles, redirectAuthenticatedTo } = options;

    const router = useRouter();
    const { user, status } = useAuth();

    useEffect(() => {
        if (status === 'loading') {
            return;
        }

        if (requireAuth && status === 'unauthenticated') {
            router.replace('/login');
            return;
        }

        if (redirectAuthenticatedTo && status === 'authenticated' && user) {
            router.replace(
                redirectAuthenticatedTo === '/dashboard' ? getDashboardPathByRole(user.role) : redirectAuthenticatedTo,
            );

            return;
        }

        if (allowedRoles && user && !allowedRoles.includes(user.role)) {
            router.replace(getDashboardPathByRole(user.role));
        }
    }, [allowedRoles, redirectAuthenticatedTo, requireAuth, router, status, user]);

    return {
        user,
        status,
        isAuthenticated: status === 'authenticated',
        isLoading: status === 'loading',
    };
}
