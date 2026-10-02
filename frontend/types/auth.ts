export type Role = 'ADMIN' | 'STUDENT' | 'INSTRUCTOR';

export type LoginRequest = {
    username: string;
    password: string;
};

export type LoginData = LoginRequest; // alias

export type LoginResponse = {
    accessToken: string;
    user: AuthUser;
};

export type AuthUser = {
    id: string;
    username: string;
    email: string;
    role: Role;
};
