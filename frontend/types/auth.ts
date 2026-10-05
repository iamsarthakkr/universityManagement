export type Role = 'ADMIN' | 'STUDENT' | 'INSTRUCTOR';

export type LoginRequest = {
    username: string;
    password: string;
};

export type LoginResponse = {
    accessToken: string;
    user: AuthUser;
};

export type AuthUser = {
    id: number;
    username: string;
    role: Role;
};
