import { ClockIcon } from 'lucide-react';

export function ComingSoonPanel({ description }: { description: string }) {
    return (
        <section className="flex flex-col items-center rounded-3xl border border-dashed border-border bg-surface-muted/60 p-10 text-center">
            <div className="flex size-10 items-center justify-center rounded-full bg-brand-soft text-brand">
                <ClockIcon className="size-5" />
            </div>
            <h2 className="mt-4 text-lg font-semibold text-text">Coming soon</h2>
            <p className="mt-1 max-w-md text-sm leading-6 text-text-muted">{description}</p>
        </section>
    );
}
