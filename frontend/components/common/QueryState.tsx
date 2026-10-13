import { UseQueryResult } from '@tanstack/react-query';
import { CircleAlertIcon, InboxIcon } from 'lucide-react';
import { ReactNode } from 'react';

import { Button } from '@/components/ui/base/button';
import { Skeleton } from '@/components/ui/base/skeleton';

type Props = {
    query: UseQueryResult<unknown, Error>;
    isEmpty: boolean;
    emptyMessage: string;
    children: ReactNode;
};

function StateMessage({ children }: { children: ReactNode }) {
    return <div className="flex flex-col items-center gap-3 px-6 py-10 text-center">{children}</div>;
}

export function QueryState({ query, isEmpty, emptyMessage, children }: Props) {
    if (query.isPending) {
        return (
            <div role="status" aria-label="Loading" className="flex flex-col gap-3 p-6">
                <Skeleton className="h-5 w-1/3" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-2/3" />
            </div>
        );
    }

    if (query.isError) {
        return (
            <StateMessage>
                <CircleAlertIcon className="size-6 text-destructive" />
                <p className="text-sm text-text">{query.error.message}</p>
                <Button variant="outline" size="sm" onClick={() => query.refetch()}>
                    Try again
                </Button>
            </StateMessage>
        );
    }

    if (isEmpty) {
        return (
            <StateMessage>
                <InboxIcon className="size-6 text-text-muted" />
                <p className="text-sm text-text-muted">{emptyMessage}</p>
            </StateMessage>
        );
    }

    return children;
}
