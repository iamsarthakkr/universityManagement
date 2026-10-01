'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useApi } from '@/context/ApiContext';
import { setUnauthorizedHandler } from '@/lib/http';
import { AuthUser } from '@/types/auth';
import { toast } from 'sonner';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
    user: AuthUser | null;
    token: string | null;
    status: AuthStatus;
    isAuthenticated: boolean;
    login: (username: string, password: string) => Promise<boolean>;
    logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

// Redirects are not done here: the layout guards (useAuthRedirect) react to `status`
// changes and navigate with `replace`, so login/logout/expiry all redirect the same way.
export function AuthProvider({ children }: { children: React.ReactNode }) {
    const api = useApi();

    const [user, setUser] = useState<AuthUser | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [status, setStatus] = useState<AuthStatus>('loading');

    const clearSession = useCallback(() => {
        localStorage.removeItem('accessToken');
        setToken(null);
        setUser(null);
        setStatus('unauthenticated');
    }, []);

    useEffect(() => {
        setUnauthorizedHandler(() => {
            clearSession();
            // Fixed id: several requests failing with 401 at once still show a single toast.
            toast.error('Your session has expired. Please log in again.', { id: 'session-expired' });
        });

        return () => setUnauthorizedHandler(null);
    }, [clearSession]);

    useEffect(() => {
        // StrictMode runs this effect twice in dev; only the latest run may update state.
        let cancelled = false;

        const initializeAuth = async () => {
            const storedToken = localStorage.getItem('accessToken');

            if (!storedToken) {
                setStatus('unauthenticated');
                return;
            }

            setToken(storedToken);

            const res = await api.auth.me();
            if (cancelled) {
                return;
            }

            if (res.isSuccess && res.body) {
                setUser(res.body);
                setStatus('authenticated');
                return;
            }

            // A 401 has already cleared the stored token via the unauthorized handler.
            // Any other failure (backend down, 5xx) keeps the token so a later reload can restore the session.
            setToken(null);
            setUser(null);
            setStatus('unauthenticated');
        };

        initializeAuth();

        return () => {
            cancelled = true;
        };
    }, [api]);

    const login = useCallback(
        async (username: string, password: string) => {
            const response = await api.auth.login({
                username,
                password,
            });
            if (!response.isSuccess || !response.body) {
                return false;
            }

            const { accessToken, user } = response.body;

            localStorage.setItem('accessToken', accessToken);
            setToken(accessToken);
            setUser(user);
            setStatus('authenticated');

            toast.success('Login successful');
            return true;
        },
        [api],
    );

    const logout = useCallback(() => {
        clearSession();
        toast.success('Logged out');
    }, [clearSession]);

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            token,
            status,
            isAuthenticated: status === 'authenticated',
            login,
            logout,
        }),
        [user, token, status, login, logout],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used inside AuthProvider');
    }

    return context;
}
