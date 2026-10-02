import { Outlet } from 'react-router';

import { AppGate } from '@/components/common/AppGate';
import { Toaster } from '@/components/ui/base/toaster';
import { TooltipProvider } from '@/components/ui/base/tooltip';

export default function RootLayout() {
    return (
        <TooltipProvider>
            <Toaster position="bottom-right" closeButton />
            <AppGate>
                <Outlet />
            </AppGate>
        </TooltipProvider>
    );
}
