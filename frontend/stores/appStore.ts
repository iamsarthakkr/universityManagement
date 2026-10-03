import { toast } from 'sonner';
import { create } from 'zustand';

import { queryClient } from '@/lib/query';
import { clearToken, getToken, setToken } from '@/lib/session';
import { useApiStore } from '@/stores/apiStore';
import { AppState, AppStoreActions, AppStoreData } from '@/types/app';

type AppStore = AppStoreData & {
    actions: AppStoreActions;
};

export const useAppStore = create<AppStore>()((set) => ({
    appState: AppState.LOADING,
    error: null,
    user: null,
    staticData: {
        departments: [],
    },

    actions: {
        init: async () => {
            set({ appState: AppState.LOADING, error: null });

            const { api } = useApiStore.getState();
            const hasToken = getToken() !== null;

            const [departmentsRes, meRes] = await Promise.all([
                api.staticData.getDepartments(),
                hasToken ? api.auth.me() : Promise.resolve(null),
            ]);

            if (!departmentsRes.isSuccess || !departmentsRes.body) {
                set({
                    appState: AppState.FAILED,
                    error: departmentsRes.message || 'Failed to load application data.',
                });
                return;
            }

            if (meRes && !meRes.isSuccess && meRes.status !== 401) {
                set({
                    appState: AppState.FAILED,
                    error: meRes.message || 'Failed to restore your session.',
                });
                return;
            }

            set({
                appState: AppState.READY,
                user: meRes?.isSuccess && meRes.body ? meRes.body : null,
                staticData: {
                    departments: departmentsRes.body,
                },
            });
        },

        login: async (username, password) => {
            const { api } = useApiStore.getState();
            const res = await api.auth.login({ username, password });

            if (!res.isSuccess || !res.body) {
                return { success: false, message: res.message || 'Login failed.' };
            }

            if (!setToken(res.body.accessToken)) {
                return {
                    success: false,
                    message:
                        'Your browser is blocking site storage, so you cannot stay signed in. Allow site data for this site and try again.',
                };
            }

            set({ user: res.body.user });
            toast.success('Login successful');
            return { success: true, message: res.message };
        },

        logout: () => {
            const removed = clearToken();
            queryClient.clear();
            set({ user: null });
            toast.success('Logged out');
            if (!removed) {
                toast.warning(
                    "Your browser blocked clearing the saved session. Clear this site's data to fully sign out on this device.",
                );
            }
        },

        expireSession: () => {
            clearToken();
            queryClient.clear();
            set({ user: null });
            toast.error('Your session has expired. Please log in again.', { id: 'session-expired' });
        },
    },
}));

export const useAppActions = () => useAppStore((state) => state.actions);
