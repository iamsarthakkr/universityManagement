import { ReactNode } from 'react';

export function DetailItem({ label, children }: { label: string; children: ReactNode }) {
    return (
        <div className="flex flex-col gap-1">
            <dt className="text-xs font-medium uppercase tracking-wide text-text-muted">{label}</dt>
            <dd className="text-sm text-text">{children}</dd>
        </div>
    );
}
