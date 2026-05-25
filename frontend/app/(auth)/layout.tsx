'use client';

import { UNIV_NAME } from '@/config/common';
import { useAuthRedirect } from '@/hooks/useAuthRedirect';
import Link from 'next/link';

export default function AuthLayout({ children }: { children: React.ReactNode }) {
    const { isLoading, isAuthenticated } = useAuthRedirect({
        redirectAuthenticatedTo: '/dashboard',
    });
    if (isLoading || isAuthenticated) {
        return null;
    }
    return (
        <main className="grid min-h-screen lg:grid-cols-[1.05fr_0.95fr]">
            <section className="hidden card-grid-bg bg-brand-soft p-10 lg:flex lg:flex-col lg:justify-between">
                <Link href="/" className="text-lg font-black tracking-tight text-brand-dark">
                    {UNIV_NAME}
                </Link>
                <div className="max-w-xl">
                    <p className="mb-4 inline-flex rounded-full bg-white/80 px-4 py-2 text-sm font-semibold text-brand-dark shadow-sm">
                        Spring Boot backend ready
                    </p>
                    <h1 className="text-5xl font-black leading-tight tracking-tight text-slate-950">
                        Manage registrations, approvals, courses, and enrollments from one clean dashboard.
                    </h1>
                    <p className="mt-6 text-lg leading-8 text-slate-600">
                        Designed around your current login and registration flow, with placeholders for admin review,
                        student, and instructor modules.
                    </p>
                </div>
                <p className="text-sm text-slate-500">
                    JWT auth integration can be added once backend endpoints are finalized.
                </p>
            </section>
            <section className="flex items-center justify-center bg-brand-soft px-5 py-10">{children}</section>
        </main>
    );
}
