export type Role = 'ADMIN' | 'STUDENT' | 'INSTRUCTOR';

export type AuthUser = {
    id: string;
    username: string;
    email: string;
    role: Role;
};
