import { ComponentProps } from 'react';

import { cn } from '@/lib/cn';

export function FilterBar({ className, ...props }: ComponentProps<'div'>) {
    return <div className={cn('grid gap-3 sm:flex sm:flex-wrap sm:items-end', className)} {...props} />;
}
