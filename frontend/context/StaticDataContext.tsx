'use client';

import React from 'react';
import { Department } from '@/types/department';
import { useApi } from './ApiContext';

type StaticDataContextValue = {
    departments: Department[];
    isLoading: boolean;
};

const StaticDataContext = React.createContext<StaticDataContextValue>({
    departments: [],
    isLoading: true,
});

export function StaticDataProvider({ children }: { children: React.ReactNode }) {
    const api = useApi();
    const [departments, setDepartments] = React.useState<Department[]>([]);
    const [isLoading, setIsLoading] = React.useState(true);

    React.useEffect(() => {
        api.staticData.getDepartments().then((res) => {
            if (res.isSuccess && res.body) {
                setDepartments(res.body);
            }
            setIsLoading(false);
        });
    }, [api]);

    return (
        <StaticDataContext.Provider value={{ departments, isLoading }}>
            {children}
        </StaticDataContext.Provider>
    );
}

export function useStaticData() {
    return React.useContext(StaticDataContext);
}
