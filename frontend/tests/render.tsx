import { QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { ReactElement } from 'react';
import { createMemoryRouter, RouterProvider } from 'react-router';

import { TooltipProvider } from '@/components/ui/base/tooltip';
import { queryClient } from '@/lib/query';
import { routes } from '@/router';

export function renderWithProviders(ui: ReactElement, { path = '/' }: { path?: string } = {}) {
    const router = createMemoryRouter([{ path: '*', element: ui }], { initialEntries: [path] });

    return render(
        <QueryClientProvider client={queryClient}>
            <TooltipProvider>
                <RouterProvider router={router} />
            </TooltipProvider>
        </QueryClientProvider>,
    );
}

export function renderApp(path: string) {
    const router = createMemoryRouter(routes, { initialEntries: [path] });
    render(<RouterProvider router={router} />);
    return router;
}
