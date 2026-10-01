'use client';

import { useEffect } from 'react';
import { useNavigate } from 'react-router';

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

    const navigate = useNavigate();
    const { user, status } = useAuth();

    const allowedRolesKey = allowedRoles?.join(',');

    useEffect(() => {
        if (status === 'loading') {
            return;
        }

        if (requireAuth && status === 'unauthenticated') {
            navigate('/login', { replace: true });
            return;
        }

        if (redirectAuthenticatedTo && status === 'authenticated' && user) {
            navigate(
                redirectAuthenticatedTo === '/dashboard' ? getDashboardPathByRole(user.role) : redirectAuthenticatedTo,
                { replace: true },
            );

            return;
        }

        if (user && allowedRolesKey && !allowedRolesKey.includes(user.role)) {
            navigate(getDashboardPathByRole(user.role), { replace: true });
        }
    }, [allowedRolesKey, redirectAuthenticatedTo, requireAuth, navigate, status, user]);

    return {
        user,
        status,
        isAuthenticated: status === 'authenticated',
        isLoading: status === 'loading',
    };
}
