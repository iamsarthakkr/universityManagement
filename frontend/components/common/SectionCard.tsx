import { ComponentProps } from 'react';

import { cn } from '@/lib/cn';

export function SectionCard({ className, ...props }: ComponentProps<'section'>) {
    return (
        <section className={cn('rounded-3xl border border-border bg-surface p-6 shadow-soft', className)} {...props} />
    );
}
