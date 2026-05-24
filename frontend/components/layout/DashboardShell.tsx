import { Sidebar } from './Sidebar';

export function DashboardShell({ children }: { children: React.ReactNode }) {
    return (
        <main className="min-h-screen bg-bg lg:grid lg:grid-cols-[280px_1fr]">
            <Sidebar />
            <section className="min-w-0 px-5 py-6 md:px-8 lg:px-10">
                <div className="mb-6 flex items-center justify-between rounded-2xl border border-border bg-white px-4 py-3 shadow-sm lg:hidden">
                    <strong className="text-brand-dark">UniEnroll</strong>
                    <span className="text-sm text-text-muted">Dashboard</span>
                </div>
                {children}
            </section>
        </main>
    );
}
