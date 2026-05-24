import React from 'react';
import Link from 'next/link';

import { cn } from '@/lib/cn';
import { Button } from '@/components/ui/base/button';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../ui/base/card';

export const LoginForm = ({ className, ...props }: React.ComponentProps<'div'>) => {
    return (
        <div className={cn('flex flex-col gap-6', className)} {...props}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Welcome back</CardTitle>
                    <CardDescription>Login with your credentials</CardDescription>
                </CardHeader>
                <CardContent>
                    <form>
                        <FieldGroup>
                            <Field>
                                <FieldLabel htmlFor="username">Username</FieldLabel>
                                <Input id="username" type="username" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="password">Password</FieldLabel>
                                <Input id="password" type="password" required />
                            </Field>
                            <Field>
                                <Button type="submit">Login</Button>
                            </Field>
                        </FieldGroup>
                    </form>
                </CardContent>
                <CardFooter>
                    <div className="grid gap-3 mx-auto rounded-2xl bg-surface-muted p-4 text-sm text-text-muted">
                        <p className="font-semibold text-text">Registration requests</p>
                        <div className="flex flex-wrap gap-2">
                            <Link className="font-semibold text-brand" href="/registration/student">
                                Student registration
                            </Link>
                            <span>·</span>
                            <Link className="font-semibold text-brand" href="/registration/instructor">
                                Instructor registration
                            </Link>
                        </div>
                    </div>
                </CardFooter>
            </Card>
        </div>
    );
};
