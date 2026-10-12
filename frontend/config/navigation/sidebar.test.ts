import { describe, expect, it } from 'vitest';

import { Role } from '@/types/auth';
import { SidebarNavEntry } from '@/types/navigation';
import { getSidebarNav } from './sidebar';

function urls(entries: SidebarNavEntry[]) {
    return entries.flatMap((entry) => ('items' in entry ? entry.items.map((item) => item.url) : [entry.url]));
}

describe('getSidebarNav', () => {
    it('gives admins registrations, academic pages and people', () => {
        expect(urls(getSidebarNav('ADMIN'))).toEqual([
            '/dashboard/admin',
            '/dashboard/admin/student-registrations',
            '/dashboard/admin/instructor-registrations',
            '/dashboard/admin/semesters',
            '/dashboard/admin/offerings',
            '/dashboard/courses',
            '/dashboard/admin/students',
            '/dashboard/admin/instructors',
        ]);
    });

    it('gives instructors their offerings and the catalogue only', () => {
        expect(urls(getSidebarNav('INSTRUCTOR'))).toEqual([
            '/dashboard/instructor',
            '/dashboard/instructor/courses',
            '/dashboard/courses',
        ]);
    });

    it('gives students enrollment, their courses and the catalogue only', () => {
        expect(urls(getSidebarNav('STUDENT'))).toEqual([
            '/dashboard/student',
            '/dashboard/student/offerings',
            '/dashboard/student/courses',
            '/dashboard/student/enrollments',
            '/dashboard/courses',
        ]);
    });

    it.each<Role>(['INSTRUCTOR', 'STUDENT'])('never shows admin-only links to %s', (role) => {
        const links = urls(getSidebarNav(role));

        expect(links.some((url) => url.startsWith('/dashboard/admin'))).toBe(false);
        expect(links).not.toContain('/dashboard/courses/new');
    });
});
