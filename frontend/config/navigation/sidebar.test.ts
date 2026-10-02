import { describe, expect, it } from 'vitest';

import { Role } from '@/types/auth';
import { SidebarNavEntry } from '@/types/navigation';
import { getSidebarNav } from './sidebar';

function urls(entries: SidebarNavEntry[]) {
    return entries.flatMap((entry) => ('items' in entry ? entry.items.map((item) => item.url) : [entry.url]));
}

describe('getSidebarNav', () => {
    it('gives admins registrations, the catalogue and course creation', () => {
        expect(urls(getSidebarNav('ADMIN'))).toEqual([
            '/dashboard/admin',
            '/dashboard/admin/student-registrations/pending',
            '/dashboard/admin/student-registrations/approved',
            '/dashboard/admin/student-registrations/rejected',
            '/dashboard/admin/instructor-registrations/pending',
            '/dashboard/admin/instructor-registrations/approved',
            '/dashboard/admin/instructor-registrations/rejected',
            '/dashboard/courses',
            '/dashboard/courses/new',
        ]);
    });

    it('gives instructors their courses and the catalogue only', () => {
        expect(urls(getSidebarNav('INSTRUCTOR'))).toEqual([
            '/dashboard/instructor',
            '/dashboard/courses',
            '/dashboard/instructor/courses',
        ]);
    });

    it('gives students their courses, enrollments and the catalogue only', () => {
        expect(urls(getSidebarNav('STUDENT'))).toEqual([
            '/dashboard/student',
            '/dashboard/courses',
            '/dashboard/student/courses',
            '/dashboard/student/enrollments',
        ]);
    });

    it.each<Role>(['INSTRUCTOR', 'STUDENT'])('never shows admin-only links to %s', (role) => {
        const links = urls(getSidebarNav(role));

        expect(links.some((url) => url.startsWith('/dashboard/admin'))).toBe(false);
        expect(links).not.toContain('/dashboard/courses/new');
    });
});
