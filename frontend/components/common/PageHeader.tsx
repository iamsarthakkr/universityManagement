export function PageHeader({ title, description }: { title: string; description: string }) {
    return (
        <div className="mb-6 flex flex-col gap-2">
            <h1 className="text-2xl font-bold tracking-tight text-text md:text-4xl">{title}</h1>
            <p className="max-w-2xl text-sm leading-6 text-text-muted md:text-base">{description}</p>
        </div>
    );
}
