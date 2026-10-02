import { ChevronRightIcon, LucideIcon } from 'lucide-react';
import { Link } from 'react-router';

export type QuickLink = {
    title: string;
    description: string;
    url: string;
    icon: LucideIcon;
};

export function QuickLinks({ links }: { links: QuickLink[] }) {
    return (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {links.map(({ title, description, url, icon: Icon }) => (
                <Link
                    key={url}
                    to={url}
                    className="group flex items-start gap-4 rounded-2xl border border-border bg-surface p-5 shadow-soft transition-colors hover:border-brand/40 hover:bg-brand-soft/40"
                >
                    <div className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-brand-soft text-brand">
                        <Icon className="size-5" />
                    </div>
                    <div className="flex-1">
                        <p className="font-semibold text-text">{title}</p>
                        <p className="mt-1 text-sm text-text-muted">{description}</p>
                    </div>
                    <ChevronRightIcon className="mt-0.5 size-4 text-text-muted transition-transform group-hover:translate-x-0.5" />
                </Link>
            ))}
        </div>
    );
}
