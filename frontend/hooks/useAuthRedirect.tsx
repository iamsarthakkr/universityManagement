'use client';

import { useEffect } from 'react';
import { useNavigate } from 'react-router';

import { DASHBOARD_HOME } from '@/config/navigation/dashboardHome';
import { useAuth } from '@/context/AuthContext';

type UseAuthRedirectOptions = {
    requireAuth?: boolean;
    redirectAuthenticated?: boolean;
};

export function useAuthRedirect(options: UseAuthRedirectOptions = {}) {
    const { requireAuth = false, redirectAuthenticated = false } = options;

    const navigate = useNavigate();
    const { user, status } = useAuth();

    useEffect(() => {
        if (status === 'loading') {
            return;
        }

        if (requireAuth && status === 'unauthenticated') {
            navigate('/login', { replace: true });
            return;
        }

        if (redirectAuthenticated && status === 'authenticated' && user) {
            navigate(DASHBOARD_HOME[user.role], { replace: true });
        }
    }, [redirectAuthenticated, requireAuth, navigate, status, user]);

    return {
        user,
        status,
        isAuthenticated: status === 'authenticated',
        isLoading: status === 'loading',
    };
}
