import Link from 'next/link';
import { cn } from '@/lib/cn';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/base/card';
import { FieldDescription } from '@/components/ui/base/field';

interface Props {
    title: string;
    description: string;
    children: React.ReactNode;
    className?: string;
}

export default function RegistrationLayout(props: Props) {
    const { className, title, description, children } = props;

    return (
        <div className={cn('flex flex-col gap-6', className)}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">{title}</CardTitle>
                    <CardDescription>{description}</CardDescription>
                </CardHeader>

                <CardContent>{children}</CardContent>
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
}
