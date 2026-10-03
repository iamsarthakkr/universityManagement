import { describe, expect, it } from 'vitest';

import { setPrefersDark } from '@/tests/setup';
import { useThemeStore } from './themeStore';

const setTheme = (theme: 'light' | 'dark' | 'system') => useThemeStore.getState().actions.setTheme(theme);
const isDark = () => document.documentElement.classList.contains('dark');

describe('themeStore', () => {
    it('defaults to system', () => {
        expect(useThemeStore.getState().theme).toBe('system');
    });

    it('applies the dark class for dark and removes it for light', () => {
        setTheme('dark');
        expect(isDark()).toBe(true);

        setTheme('light');
        expect(isDark()).toBe(false);
    });

    it('follows the OS preference on system', () => {
        setPrefersDark(true);
        setTheme('light');
        setTheme('system');
        expect(isDark()).toBe(true);

        setPrefersDark(false);
        setTheme('dark');
        setTheme('system');
        expect(isDark()).toBe(false);
    });

    it('persists the choice under the key the index.html script reads', () => {
        setTheme('dark');

        expect(JSON.parse(localStorage.getItem('theme') ?? 'null')).toMatchObject({ state: { theme: 'dark' } });
    });
});
