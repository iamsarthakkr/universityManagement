import type { Department } from './department';

export type Role = 'ADMIN' | 'STUDENT' | 'INSTRUCTOR';

export type LoginRequest = {
    username: string;
    password: string;
};

export type LoginResponse = {
    accessToken: string;
    user: Me;
};

export type Profile = {
    id: number;
    firstName: string;
    lastName: string | null;
    department: Department;
};

export type Me = {
    id: number;
    username: string;
    email: string;
    role: Role;
    // null for admins.
    profile: Profile | null;
};
