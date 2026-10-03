import { QueryClientProvider } from '@tanstack/react-query';
import { Outlet } from 'react-router';

import { AppGate } from '@/components/common/AppGate';
import { Toaster } from '@/components/ui/base/toaster';
import { TooltipProvider } from '@/components/ui/base/tooltip';
import { queryClient } from '@/lib/query';

export default function RootLayout() {
    return (
        <QueryClientProvider client={queryClient}>
            <TooltipProvider>
                <Toaster position="bottom-right" closeButton />
                <AppGate>
                    <Outlet />
                </AppGate>
            </TooltipProvider>
        </QueryClientProvider>
    );
}
