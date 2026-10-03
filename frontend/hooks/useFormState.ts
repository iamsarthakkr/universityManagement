import { ChangeEvent, useCallback, useState } from 'react';

export function useFormState<T extends object>(initialValues: T) {
    const [values, setValues] = useState<T>(initialValues);

    const handleChange = useCallback((event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value, type } = event.target;
        setValues((prev) => ({
            ...prev,
            [name]: type === 'number' ? Number(value) : value,
        }));
    }, []);

    const setField = useCallback(<K extends keyof T>(name: K, value: T[K]) => {
        setValues((prev) => ({ ...prev, [name]: value }));
    }, []);

    const reset = useCallback(() => setValues(initialValues), [initialValues]);

    return { values, handleChange, setField, reset };
}
