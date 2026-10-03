import { Navigate, Outlet } from 'react-router';

import { DASHBOARD_HOME } from '@/config/navigation/dashboardHome';
import { useAppStore } from '@/stores/appStore';
import { Role } from '@/types/auth';

export function RoleGuard({ roles }: { roles: Role[] }) {
    const user = useAppStore((state) => state.user);

    if (!user) {
        return null;
    }

    if (!roles.includes(user.role)) {
        return <Navigate to={DASHBOARD_HOME[user.role]} replace />;
    }

    return <Outlet />;
}

export function DashboardHomeRedirect() {
    const user = useAppStore((state) => state.user);

    if (!user) {
        return null;
    }

    return <Navigate to={DASHBOARD_HOME[user.role]} replace />;
}
