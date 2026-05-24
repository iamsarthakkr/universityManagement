'use client';

import { ApiProvider } from '@/context/ApiContext';
import { AuthProvider } from './AuthContext';

export function Providers({ children }: { children: React.ReactNode }) {
    return (
        <ApiProvider>
            <AuthProvider>{children}</AuthProvider>
        </ApiProvider>
    );
}
