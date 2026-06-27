import React from 'react';

export function CourseFormLayout({ children }: { children: React.ReactNode }) {
    return (
        <section className="rounded-3xl border border-border bg-white p-6 shadow-soft">
            <div className="flex flex-col gap-8">{children}</div>
        </section>
    );
}

export function CourseFormSection({
    label,
    description,
    children,
}: {
    label: string;
    description: string;
    children: React.ReactNode;
}) {
    return (
        <div className="grid gap-6 border-b border-border pb-8 last:border-0 last:pb-0 md:grid-cols-[1fr_2fr]">
            <div>
                <h3 className="text-sm font-semibold text-text">{label}</h3>
                <p className="mt-1 text-sm text-text-muted">{description}</p>
            </div>
            <div>{children}</div>
        </div>
    );
}
