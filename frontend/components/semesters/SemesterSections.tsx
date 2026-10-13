import { ReactNode } from 'react';

import { formatSemesterName, SemesterGroup } from '@/lib/semester';
import { SemesterStatusBadge } from './SemesterStatusBadge';

type Props<T> = {
    groups: SemesterGroup<T>[];
    itemKey: (item: T) => number;
    renderItem: (item: T) => ReactNode;
    renderMeta?: (group: SemesterGroup<T>) => ReactNode;
};

export function SemesterSections<T>({ groups, itemKey, renderItem, renderMeta }: Props<T>) {
    return (
        <div className="flex flex-col gap-8">
            {groups.map((group) => (
                <section
                    key={group.semester.id}
                    aria-label={formatSemesterName(group.semester)}
                    className="flex flex-col gap-3"
                >
                    <div className="flex flex-wrap items-center gap-3">
                        <h2 className="text-lg font-semibold text-text">{formatSemesterName(group.semester)}</h2>
                        <SemesterStatusBadge status={group.semester.status} />
                        {renderMeta?.(group)}
                    </div>
                    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                        {group.items.map((item) => (
                            <div key={itemKey(item)} className="flex">
                                {renderItem(item)}
                            </div>
                        ))}
                    </div>
                </section>
            ))}
        </div>
    );
}
