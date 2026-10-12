import { MoreHorizontalIcon } from 'lucide-react';
import React from 'react';

import { Button } from '@/components/ui/base/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/base/dropdownMenu';
import { ConfirmDialog } from './ConfirmDialog';

export type ActionConfig = {
    label: string;
    destructive?: boolean;
    confirm?: {
        title: (subject: string) => string;
        description: string;
        cancelLabel: string;
        confirmLabel?: string;
    };
};

type Props<A extends string> = {
    // Straight from the server's `allowedActions`; the menu never adds or filters actions.
    actions: A[];
    config: Record<A, ActionConfig>;
    // What the actions apply to, e.g. "Winter 2026"; names the menu button and the confirmation.
    subject: string;
    disabled?: boolean;
    onAction: (action: A) => void;
};

export function ActionMenu<A extends string>({ actions, config, subject, disabled = false, onAction }: Props<A>) {
    const [confirming, setConfirming] = React.useState<A | null>(null);

    if (actions.length === 0) {
        return null;
    }

    const select = (action: A) => {
        if (config[action].confirm) {
            setConfirming(action);
            return;
        }
        onAction(action);
    };

    const confirmConfig = confirming ? config[confirming] : null;

    return (
        <>
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon" className="size-8" disabled={disabled}>
                        <MoreHorizontalIcon className="size-4" />
                        <span className="sr-only">Actions for {subject}</span>
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                    {actions.map((action) => (
                        <DropdownMenuItem
                            key={action}
                            variant={config[action].destructive ? 'destructive' : 'default'}
                            onClick={() => select(action)}
                        >
                            {config[action].label}
                        </DropdownMenuItem>
                    ))}
                </DropdownMenuContent>
            </DropdownMenu>
            {confirming && confirmConfig?.confirm && (
                <ConfirmDialog
                    open
                    title={confirmConfig.confirm.title(subject)}
                    description={confirmConfig.confirm.description}
                    confirmLabel={confirmConfig.confirm.confirmLabel ?? confirmConfig.label}
                    cancelLabel={confirmConfig.confirm.cancelLabel}
                    destructive={confirmConfig.destructive}
                    onConfirm={() => {
                        setConfirming(null);
                        onAction(confirming);
                    }}
                    onCancel={() => setConfirming(null)}
                />
            )}
        </>
    );
}
