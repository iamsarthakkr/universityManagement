'use client';

import { ApiProvider } from '@/context/ApiContext';
import { AuthProvider } from './AuthContext';
import { AppProvider } from './AppContext';
import { TooltipProvider } from '@/components/ui/base/tooltip';
import { Toaster } from '@/components/ui/base/toaster';

export function Providers({ children }: { children: React.ReactNode }) {
    return (
        <TooltipProvider>
            <Toaster position="bottom-right" closeButton />
            <ApiProvider>
                <AuthProvider>
                    <AppProvider>{children}</AppProvider>
                </AuthProvider>
            </ApiProvider>
        </TooltipProvider>
    );
}
