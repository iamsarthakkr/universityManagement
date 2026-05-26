import Link from 'next/link';

import { cn } from '@/lib/cn';
import { Button } from '@/components/ui/base/button';
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../ui/base/card';

export const StudentRegistrationForm = ({ className, ...props }: React.ComponentProps<'div'>) => {
    return (
        <div className={cn('flex flex-col gap-6', className)} {...props}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Student registration</CardTitle>
                    <CardDescription>
                        Creates a pending student registration request for admin approval.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form>
                        <FieldGroup>
                            <div className="grid gap-4 md:grid-cols-2">
                                <Field>
                                    <FieldLabel htmlFor="firstName">Firstname</FieldLabel>
                                    <Input id="firstName" type="text" required />
                                </Field>
                                <Field>
                                    <FieldLabel htmlFor="lastname">Lastname</FieldLabel>
                                    <Input id="lastname" type="text" />
                                </Field>
                            </div>
                            <Field>
                                <FieldLabel htmlFor="email">Email</FieldLabel>
                                <Input id="email" type="text" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="username">Username</FieldLabel>
                                <Input id="username" type="username" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="password">Password</FieldLabel>
                                <Input id="password" type="password" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="phoneNumber">Phone number</FieldLabel>
                                <Input id="phoneNumber" type="text" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="dateOfBirth">Date of Birth</FieldLabel>
                                <Input id="dateOfBirth" type="date" required />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="address">Address</FieldLabel>
                                <Input id="address" type="text" required />
                            </Field>
                            <Field>
                                <Button type="button" className="mt-2 w-full">
                                    Submit request
                                </Button>
                            </Field>
                        </FieldGroup>
                    </form>
                </CardContent>
                <CardFooter>
                    <FieldDescription className="mx-auto px-6 text-center">
                        <p className="text-center text-sm text-text-muted">
                            Already registered?{' '}
                            <Link className="font-semibold text-brand" href="/login">
                                Login
                            </Link>
                        </p>
                    </FieldDescription>
                </CardFooter>
            </Card>
        </div>
    );
};
