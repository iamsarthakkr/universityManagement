import { ChangeEvent } from 'react';

import { Field, FieldLabel } from '@/components/ui/base/field';
import { Input } from '@/components/ui/base/input';

type AccountValues = {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName?: string;
};

type Props = {
    values: AccountValues;
    onChange: (event: ChangeEvent<HTMLInputElement>) => void;
};

export function AccountFields({ values, onChange }: Props) {
    return (
        <>
            <Field>
                <FieldLabel htmlFor="username">Username</FieldLabel>
                <Input
                    id="username"
                    name="username"
                    type="text"
                    autoComplete="username"
                    required
                    value={values.username}
                    onChange={onChange}
                />
            </Field>

            <Field>
                <FieldLabel htmlFor="password">Password</FieldLabel>
                <Input
                    id="password"
                    name="password"
                    type="password"
                    autoComplete="new-password"
                    required
                    value={values.password}
                    onChange={onChange}
                />
            </Field>

            <Field>
                <FieldLabel htmlFor="email">Email</FieldLabel>
                <Input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    required
                    value={values.email}
                    onChange={onChange}
                />
            </Field>

            <div className="grid gap-4 md:grid-cols-2">
                <Field>
                    <FieldLabel htmlFor="firstName">First name</FieldLabel>
                    <Input
                        id="firstName"
                        name="firstName"
                        type="text"
                        autoComplete="given-name"
                        required
                        value={values.firstName}
                        onChange={onChange}
                    />
                </Field>

                <Field>
                    <FieldLabel htmlFor="lastName">Last name</FieldLabel>
                    <Input
                        id="lastName"
                        name="lastName"
                        type="text"
                        autoComplete="family-name"
                        value={values.lastName ?? ''}
                        onChange={onChange}
                    />
                </Field>
            </div>
        </>
    );
}
