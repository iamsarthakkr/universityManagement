'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';

import { ErrorOverlay, LoadingOverlay } from '@/components/common/AppStateOverlay';
import { useApi } from '@/context/ApiContext';
import { useAuth } from '@/context/AuthContext';
import { AppState, StaticData } from '@/types/app';
import { AuthStatus } from '@/types/auth';

type AppContextValue = {
    staticData: StaticData;
};

function resolveAppState(staticData: StaticData | null, error: string | null, authStatus: AuthStatus): AppState {
    if (error) {
        return AppState.FAILED;
    }

    if (!staticData || authStatus === 'loading') {
        return AppState.LOADING;
    }

    return AppState.READY;
}

const AppContext = createContext<AppContextValue | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
    const api = useApi();
    const { status: authStatus } = useAuth();

    const [staticData, setStaticData] = useState<StaticData | null>(null);
    const [staticDataError, setStaticDataError] = useState<string | null>(null);
    const [loadAttempt, setLoadAttempt] = useState(0);

    useEffect(() => {
        let cancelled = false;

        const loadStaticData = async () => {
            const departmentsRes = await api.staticData.getDepartments();
            if (cancelled) {
                return;
            }

            if (!departmentsRes.isSuccess || !departmentsRes.body) {
                setStaticDataError(departmentsRes.message || 'Failed to load application data.');
                return;
            }

            setStaticData({ departments: departmentsRes.body });
        };

        loadStaticData();

        return () => {
            cancelled = true;
        };
    }, [api, loadAttempt]);

    const retry = useCallback(() => {
        setStaticDataError(null);
        setLoadAttempt((attempt) => attempt + 1);
    }, []);

    const appState = resolveAppState(staticData, staticDataError, authStatus);
    const value = useMemo<AppContextValue | null>(() => (staticData ? { staticData } : null), [staticData]);

    if (appState === AppState.FAILED) {
        return <ErrorOverlay message={staticDataError ?? 'Failed to load application data.'} onRetry={retry} />;
    }

    if (appState === AppState.LOADING || !value) {
        return <LoadingOverlay />;
    }

    return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useAppContext() {
    const context = useContext(AppContext);

    if (!context) {
        throw new Error('useAppContext must be used inside AppProvider');
    }

    return context;
}
