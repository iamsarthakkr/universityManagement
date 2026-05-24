import Link from 'next/link';
import {
    BookOpen,
    GraduationCap,
    LayoutDashboard,
    LogOut,
    ShieldCheck,
    UserRoundCheck,
    UsersRound,
} from 'lucide-react';

const nav = [
    { href: '/admin', label: 'Admin Dashboard', icon: ShieldCheck },
    {
        href: '/admin/registrations/students',
        label: 'Student Registrations',
        icon: UsersRound,
    },
    {
        href: '/admin/registrations/instructors',
        label: 'Instructor Registrations',
        icon: UserRoundCheck,
    },
    { href: '/student', label: 'Student Home', icon: GraduationCap },
    { href: '/student/courses', label: 'My Courses', icon: BookOpen },
    { href: '/instructor', label: 'Instructor Home', icon: LayoutDashboard },
];

export function Sidebar() {
    return (
        <aside className="hidden min-h-screen border-r border-border bg-white px-4 py-5 lg:block">
            <Link href="/admin" className="mb-8 flex items-center gap-2 px-2 text-lg font-black text-brand-dark">
                <span className="grid size-9 place-items-center rounded-xl bg-brand text-white">U</span>
                UniEnroll
            </Link>
            <nav className="grid gap-1">
                {nav.map((item) => {
                    const Icon = item.icon;
                    return (
                        <Link
                            key={item.href}
                            href={item.href}
                            className="flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium text-text-muted transition hover:bg-brand-soft hover:text-brand-dark"
                        >
                            <Icon size={18} />
                            {item.label}
                        </Link>
                    );
                })}
            </nav>
            <Link
                href="/login"
                className="mt-8 flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium text-text-muted hover:bg-surface-muted"
            >
                <LogOut size={18} /> Logout
            </Link>
        </aside>
    );
}
