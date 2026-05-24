import { PageHeader } from '@/components/common/PageHeader';
import { StatCard } from '@/components/ui/StatCard';
import { dashboardStats } from '@/lib/mock-data';

export default function AdminDashboardPage() {
    return (
        <>
            <PageHeader
                title="Admin dashboard"
                description="Review pending registration requests, approve users, and later manage courses, students, instructors, and enrollments."
            />
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                {dashboardStats.map((stat) => (
                    <StatCard key={stat.label} {...stat} />
                ))}
            </div>
            <section className="mt-6 rounded-3xl border border-border bg-white p-6 shadow-soft">
                <h2 className="text-xl font-bold">Next admin actions</h2>
                <div className="mt-5 grid gap-3 md:grid-cols-3">
                    {[
                        'Approve student registrations',
                        'Approve instructor registrations',
                        'Manage course assignments',
                    ].map((item) => (
                        <div
                            key={item}
                            className="rounded-2xl border border-border bg-surface-muted p-4 text-sm font-semibold text-text"
                        >
                            {item}
                        </div>
                    ))}
                </div>
            </section>
        </>
    );
}
