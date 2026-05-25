'use client';

import { ApiProvider } from '@/context/ApiContext';
import { AuthProvider } from './AuthContext';
import { TooltipProvider } from '@/components/ui/base/tooltip';

export function Providers({ children }: { children: React.ReactNode }) {
    return (
        <TooltipProvider>
            <ApiProvider>
                <AuthProvider>{children}</AuthProvider>
            </ApiProvider>
        </TooltipProvider>
    );
}
