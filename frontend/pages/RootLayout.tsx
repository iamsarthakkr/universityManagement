import { Outlet } from 'react-router';
import { Providers } from '@/context/Providers';

export default function RootLayout() {
    return (
        <Providers>
            <Outlet />
        </Providers>
    );
}
