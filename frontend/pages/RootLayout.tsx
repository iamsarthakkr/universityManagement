import { Outlet } from 'react-router';
import { Providers } from '@/context/Providers';

// Providers sit inside the router so they can use router hooks if needed.
export default function RootLayout() {
    return (
        <Providers>
            <Outlet />
        </Providers>
    );
}
