import { Role } from '@/types/auth';

export const DASHBOARD_HOME: Record<Role, string> = {
    ADMIN: '/dashboard/admin',
    STUDENT: '/dashboard/student',
    INSTRUCTOR: '/dashboard/instructor',
};
