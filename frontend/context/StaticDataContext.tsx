'use client';

import React from 'react';
import { Department } from '@/types/department';
import { Instructor } from '@/types/instructor';
import { useApi } from './ApiContext';

type StaticDataContextValue = {
    departments: Department[];
    instructors: Instructor[];
    isLoading: boolean;
};

const StaticDataContext = React.createContext<StaticDataContextValue>({
    departments: [],
    instructors: [],
    isLoading: true,
});

export function StaticDataProvider({ children }: { children: React.ReactNode }) {
    const api = useApi();
    const [departments, setDepartments] = React.useState<Department[]>([]);
    const [instructors, setInstructors] = React.useState<Instructor[]>([]);
    const [isLoading, setIsLoading] = React.useState(true);

    React.useEffect(() => {
        Promise.all([api.staticData.getDepartments(), api.staticData.getInstructors()]).then(
            ([departmentsRes, instructorsRes]) => {
                if (departmentsRes.isSuccess && departmentsRes.body) {
                    setDepartments(departmentsRes.body);
                }
                if (instructorsRes.isSuccess && instructorsRes.body) {
                    setInstructors(instructorsRes.body);
                }
                setIsLoading(false);
            },
        );
    }, [api]);

    return (
        <StaticDataContext.Provider value={{ departments, instructors, isLoading }}>
            {children}
        </StaticDataContext.Provider>
    );
}

export function useStaticData() {
    return React.useContext(StaticDataContext);
}
