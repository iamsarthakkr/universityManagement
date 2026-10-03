import { useEffect } from 'react';

import { ErrorOverlay, LoadingOverlay } from '@/components/common/AppStateOverlay';
import { setUnauthorizedHandler } from '@/lib/http';
import { useAppActions, useAppStore } from '@/stores/appStore';
import { AppState } from '@/types/app';

export function AppGate({ children }: { children: React.ReactNode }) {
    const appState = useAppStore((state) => state.appState);
    const error = useAppStore((state) => state.error);
    const { init, expireSession } = useAppActions();

    useEffect(() => {
        setUnauthorizedHandler(expireSession);
        init();

        return () => setUnauthorizedHandler(null);
    }, [init, expireSession]);

    if (appState === AppState.FAILED) {
        return <ErrorOverlay message={error ?? 'Failed to load application data.'} onRetry={init} />;
    }

    if (appState === AppState.LOADING) {
        return <LoadingOverlay />;
    }

    return children;
}
