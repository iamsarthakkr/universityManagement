import { Button } from '@/components/ui/base/button';

export function PlaceholderPanel({ title, description }: { title: string; description: string }) {
    return (
        <section className="rounded-3xl border border-dashed border-indigo-200 bg-brand-soft/60 p-8">
            <h2 className="text-xl font-bold text-text">{title}</h2>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-text-muted">{description}</p>
            <div className="mt-5 flex flex-wrap gap-3">
                <Button variant="secondary">Connect API later</Button>
                <Button variant="ghost">Keep as dummy page</Button>
            </div>
        </section>
    );
}
