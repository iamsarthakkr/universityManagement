export function StatCard({ label, value }: { label: string; value: string }) {
    return (
        <div className="rounded-2xl border border-border bg-white p-5 shadow-soft">
            <p className="text-sm text-text-muted">{label}</p>
            <p className="mt-3 text-3xl font-bold tracking-tight text-text">{value}</p>
        </div>
    );
}
