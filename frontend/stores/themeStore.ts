import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export type Theme = 'light' | 'dark' | 'system';

type ThemeStore = {
    theme: Theme;
    actions: {
        setTheme: (theme: Theme) => void;
    };
};

const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)');

function applyTheme(theme: Theme) {
    const isDark = theme === 'dark' || (theme === 'system' && darkModeQuery.matches);
    document.documentElement.classList.toggle('dark', isDark);
}

export const useThemeStore = create<ThemeStore>()(
    persist(
        (set) => ({
            theme: 'system',
            actions: {
                setTheme: (theme) => set({ theme }),
            },
        }),
        {
            name: 'theme',
            partialize: (state) => ({ theme: state.theme }),
        },
    ),
);

applyTheme(useThemeStore.getState().theme);
useThemeStore.subscribe((state) => applyTheme(state.theme));
darkModeQuery.addEventListener('change', () => applyTheme(useThemeStore.getState().theme));

export const useTheme = () => useThemeStore((state) => state.theme);
export const useThemeActions = () => useThemeStore((state) => state.actions);
