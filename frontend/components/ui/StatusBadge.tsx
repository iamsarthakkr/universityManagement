import { cn } from '@/lib/cn';

export type StatusTone = 'warning' | 'success' | 'danger' | 'info' | 'neutral';

const toneStyles: Record<StatusTone, string> = {
    warning:
        'bg-yellow-50 text-yellow-700 ring-yellow-600/20 dark:bg-yellow-400/10 dark:text-yellow-300 dark:ring-yellow-400/20',
    success:
        'bg-green-50 text-green-700 ring-green-600/20 dark:bg-green-400/10 dark:text-green-300 dark:ring-green-400/20',
    danger: 'bg-red-50 text-red-700 ring-red-600/20 dark:bg-red-400/10 dark:text-red-300 dark:ring-red-400/20',
    info: 'bg-blue-50 text-blue-700 ring-blue-600/20 dark:bg-blue-400/10 dark:text-blue-300 dark:ring-blue-400/20',
    neutral: 'bg-surface-muted text-text-muted ring-border',
};

type StatusBadgeProps = {
    tone: StatusTone;
    children: string;
};

export function StatusBadge({ tone, children }: StatusBadgeProps) {
    return (
        <span
            className={cn(
                'inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset',
                toneStyles[tone],
            )}
        >
            {children}
        </span>
    );
}
