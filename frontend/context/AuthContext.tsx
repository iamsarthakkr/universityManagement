'use client';

import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApi } from '@/context/ApiContext';
import { AuthUser } from '@/types/auth';
import { toast } from 'sonner';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
    user: AuthUser | null;
    token: string | null;
    status: AuthStatus;
    isAuthenticated: boolean;
    login: (username: string, password: string) => Promise<string | null>;
    logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const router = useRouter();
    const api = useApi();

    const [user, setUser] = useState<AuthUser | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [status, setStatus] = useState<AuthStatus>('loading');

    useEffect(() => {
        async function initializeAuth() {
            const storedToken = localStorage.getItem('accessToken');

            if (!storedToken) {
                setStatus('unauthenticated');
                return;
            }

            try {
                setToken(storedToken);

                const res = await api.auth.me();
                if (res.isSuccess && res.body) {
                    setUser(res.body);
                    setStatus('authenticated');
                } else {
                    setToken(null);
                    setUser(null);
                    setStatus('unauthenticated');
                }
            } catch {
                localStorage.removeItem('accessToken');
                setToken(null);
                setUser(null);
                setStatus('unauthenticated');
            }
        }

        initializeAuth();
    }, [api]);

    async function login(username: string, password: string) {
        const response = await api.auth.login({
            username,
            password,
        });
        if (!response.isSuccess || !response.body) {
            return 'Login failed';
        }

        const { accessToken, user } = response.body;

        localStorage.setItem('accessToken', accessToken);
        setToken(accessToken);

        setUser(user);
        setStatus('authenticated');

        if (user.role === 'ADMIN') {
            router.push('/dashboard/admin');
        } else if (user.role === 'STUDENT') {
            router.push('/dashboard/student');
        } else if (user.role === 'INSTRUCTOR') {
            router.push('/dashboard/instructor');
        }
        toast.success('Login successful');
        return null;
    }

    function logout() {
        localStorage.removeItem('accessToken');
        setToken(null);
        setUser(null);
        setStatus('unauthenticated');
        router.push('/login');
        toast.success('Logged out');
    }

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            token,
            status,
            isAuthenticated: status === 'authenticated',
            login,
            logout,
        }),
        [user, token, status],
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
