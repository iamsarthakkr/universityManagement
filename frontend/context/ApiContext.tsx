'use client';

import { createApi } from '@/lib/api/api';
import { IApi } from '@/types/IApi';
import { createContext, useContext, useMemo } from 'react';

const ApiContext = createContext<IApi | null>(null);

export function ApiProvider({ children }: { children: React.ReactNode }) {
    const value = useMemo<IApi>(() => createApi(), []);

    return <ApiContext.Provider value={value}>{children}</ApiContext.Provider>;
}

export function useApi() {
    const context = useContext(ApiContext);

    if (!context) {
        throw new Error('useApi must be used inside ApiProvider');
    }

    return context;
}
