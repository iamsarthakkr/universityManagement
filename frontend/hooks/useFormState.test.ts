import { act, renderHook } from '@testing-library/react';
import { ChangeEvent } from 'react';
import { describe, expect, it } from 'vitest';

import { useFormState } from './useFormState';

const initialValues = { title: '', credits: 1 };

function changeEvent(name: string, value: string, type = 'text') {
    return { target: { name, value, type } } as ChangeEvent<HTMLInputElement>;
}

describe('useFormState', () => {
    it('updates text fields by name', () => {
        const { result } = renderHook(() => useFormState(initialValues));

        act(() => result.current.handleChange(changeEvent('title', 'Algorithms')));

        expect(result.current.values).toEqual({ title: 'Algorithms', credits: 1 });
    });

    it('converts number inputs to numbers', () => {
        const { result } = renderHook(() => useFormState(initialValues));

        act(() => result.current.handleChange(changeEvent('credits', '4', 'number')));

        expect(result.current.values.credits).toBe(4);
    });

    it('sets a single field and resets to the initial values', () => {
        const { result } = renderHook(() => useFormState(initialValues));

        act(() => result.current.setField('credits', 3));
        expect(result.current.values.credits).toBe(3);

        act(() => result.current.reset());
        expect(result.current.values).toEqual(initialValues);
    });
});
