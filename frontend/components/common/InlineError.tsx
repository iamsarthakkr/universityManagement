import { CircleAlertIcon } from 'lucide-react';

import { Button } from '@/components/ui/base/button';

type Props = {
    message: string;
    onRetry: () => void;
};

export function InlineError({ message, onRetry }: Props) {
    return (
        <div
            role="alert"
            className="flex flex-wrap items-center gap-3 rounded-2xl border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-text"
        >
            <CircleAlertIcon className="size-4 shrink-0 text-destructive" />
            <span className="flex-1">{message}</span>
            <Button variant="outline" size="sm" onClick={onRetry}>
                Try again
            </Button>
        </div>
    );
}
