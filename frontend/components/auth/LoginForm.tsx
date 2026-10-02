import React from 'react';
import { Link } from 'react-router';

import { cn } from '@/lib/cn';
import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/base/card';
import { useAppActions } from '@/stores/appStore';
import { LoginData } from '@/types/auth';

export const LoginForm = ({ className, ...props }: React.ComponentProps<'div'>) => {
    const { login } = useAppActions();
    const [formData, setFormData] = React.useState<LoginData>({
        username: '',
        password: '',
    });
    const [errorMessage, setErrorMessage] = React.useState<string | null>(null);

    const [isLoading, setIsLoading] = React.useState(false);
    const handleChange = React.useCallback(
        (event: React.ChangeEvent<HTMLInputElement>) => {
            const { name, value } = event.target;

            setFormData((prev) => ({
                ...prev,

                [name]: value,
            }));
        },

        [],
    );

    const handleLogin = React.useCallback(
        async (event: React.SubmitEvent<HTMLFormElement>) => {
            event.preventDefault();

            setIsLoading(true);
            const result = await login(formData.username, formData.password);
            setErrorMessage(result.success ? null : result.message);
            setIsLoading(false);
        },

        [login, formData],
    );

    return (
        <div className={cn('flex flex-col gap-6', className)} {...props}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Welcome back</CardTitle>
                    <CardDescription>Login with your credentials</CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleLogin}>
                        <FieldGroup>
                            <Field>
                                <FieldLabel htmlFor="username">Username</FieldLabel>
                                <Input
                                    id="username"
                                    name="username"
                                    type="text"
                                    autoComplete="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                    required
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="password">Password</FieldLabel>
                                <Input
                                    id="password"
                                    name="password"
                                    type="password"
                                    autoComplete="current-password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    required
                                />
                            </Field>
                            {errorMessage && (
                                <p role="alert" className="text-center text-sm text-destructive">
                                    {errorMessage}
                                </p>
                            )}
                            <Field>
                                <Button type="submit" disabled={isLoading}>
                                    {isLoading ? 'Logging in...' : 'Log in'}
                                </Button>
                            </Field>
                        </FieldGroup>
                    </form>
                </CardContent>
                <CardFooter>
                    <div className="grid gap-3 mx-auto rounded-2xl bg-surface-muted p-4 text-sm text-text-muted">
                        <p className="font-semibold text-text">Registration requests</p>
                        <div className="flex flex-wrap gap-2">
                            <Link className="font-semibold text-brand" to="/registration/student">
                                Student registration
                            </Link>
                            <span>·</span>
                            <Link className="font-semibold text-brand" to="/registration/instructor">
                                Instructor registration
                            </Link>
                        </div>
                    </div>
                </CardFooter>
            </Card>
        </div>
    );
};
