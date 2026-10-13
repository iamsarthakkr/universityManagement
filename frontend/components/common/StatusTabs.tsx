import { cn } from '@/lib/cn';

export type StatusTab<T extends string> = {
    value: T | undefined;
    label: string;
};

type Props<T extends string> = {
    label: string;
    tabs: StatusTab<T>[];
    value: T | undefined;
    onChange: (value: T | undefined) => void;
};

export function StatusTabs<T extends string>({ label, tabs, value, onChange }: Props<T>) {
    return (
        <div role="tablist" aria-label={label} className="inline-flex flex-wrap gap-1 rounded-2xl bg-surface-muted p-1">
            {tabs.map((tab) => {
                const selected = tab.value === value;
                return (
                    <button
                        key={tab.label}
                        type="button"
                        role="tab"
                        aria-selected={selected}
                        onClick={() => onChange(tab.value)}
                        className={cn(
                            'rounded-xl px-3 py-1.5 text-sm font-medium transition-colors',
                            selected ? 'bg-surface text-text shadow-soft' : 'text-text-muted hover:text-text',
                        )}
                    >
                        {tab.label}
                    </button>
                );
            })}
        </div>
    );
}
