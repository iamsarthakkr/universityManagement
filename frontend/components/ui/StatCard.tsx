import { Link } from 'react-router';

type Props = {
    label: string;
    value: string;
    to?: string;
};

export function StatCard({ label, value, to }: Props) {
    const content = (
        <>
            <p className="text-sm text-text-muted">{label}</p>
            <p className="mt-3 text-3xl font-bold tracking-tight text-text">{value}</p>
        </>
    );
    const className = 'rounded-2xl border border-border bg-surface p-5 shadow-soft';

    if (to) {
        return (
            <Link
                to={to}
                className={`${className} block transition-colors hover:border-brand/40 hover:bg-brand-soft/40`}
            >
                {content}
            </Link>
        );
    }

    return <div className={className}>{content}</div>;
}
