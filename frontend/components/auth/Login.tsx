'use client';

import { LoginForm } from './LoginForm';
import { AuthShell } from '../layout/AuthShell';

export const Login = () => {
    return (
        <AuthShell>
            <LoginForm />
        </AuthShell>
    );
};
