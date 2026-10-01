import { Outlet } from 'react-router';
import { Providers } from '@/context/Providers';

// Providers must live inside the router: AuthProvider calls useNavigate().
export default function RootLayout() {
    return (
        <Providers>
            <Outlet />
        </Providers>
    );
}
