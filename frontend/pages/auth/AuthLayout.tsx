'use client';

import { ICONS, UNIV_NAME, UNIV_SHORT } from '@/config/common';
import { DASHBOARD_HOME } from '@/config/navigation/dashboardHome';
import { useAppStore } from '@/stores/appStore';
import { Link, Navigate, Outlet } from 'react-router';

export default function AuthLayout() {
    const user = useAppStore((state) => state.user);

    if (user) {
        return <Navigate to={DASHBOARD_HOME[user.role]} replace />;
    }

    return (
        <main className="grid min-h-screen lg:grid-cols-[1.05fr_0.95fr]">
            <section className="hidden card-grid-bg bg-brand-soft p-10 lg:flex lg:flex-col lg:justify-between">
                <Link to="/" className="text-lg font-black tracking-tight text-brand-dark">
                    {UNIV_NAME}
                </Link>
                <div className="max-w-xl">
                    <p className="mb-4 inline-flex rounded-full bg-white/80 px-4 py-2 text-sm font-semibold text-brand-dark shadow-sm">
                        Student and staff portal
                    </p>
                    <h2 className="text-5xl font-black leading-tight tracking-tight text-slate-950">
                        Manage registrations, approvals, courses, and enrollments from one clean dashboard.
                    </h2>
                    <p className="mt-6 text-lg leading-8 text-slate-600">
                        Students and instructors can request access here. Every registration is reviewed and approved by
                        the university administration.
                    </p>
                </div>
                <p className="text-sm text-slate-500">
                    © {new Date().getFullYear()} {UNIV_NAME}
                </p>
            </section>
            <section className="flex items-center justify-center bg-brand-soft px-5 py-10">
                <div className="flex flex-col items-center justify-center gap-6">
                    <div className="flex w-full min-w-xs max-w-sm flex-col gap-6">
                        <div className="flex flex-col items-center gap-2 font-medium">
                            <div className="flex size-8 items-center justify-center rounded-md">
                                <ICONS.mainIcon className="size-6" />
                            </div>
                            <h1 className="text-center text-xl font-bold">Welcome to {UNIV_SHORT}</h1>
                        </div>
                        <Outlet />
                    </div>
                </div>
            </section>
        </main>
    );
}
