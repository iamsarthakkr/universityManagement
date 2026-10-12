import React from 'react';
import { useSearchParams } from 'react-router';

export type ParamParser<T> = (raw: string) => T | undefined;

export const idParam: ParamParser<number> = (raw) => {
    const value = Number(raw);
    return Number.isInteger(value) && value > 0 ? value : undefined;
};

// Kept as typed, so a search box bound to it doesn't lose the space before the next word.
export const textParam: ParamParser<string> = (raw) => (raw.trim() ? raw : undefined);

export function enumParam<E extends string>(values: readonly E[]): ParamParser<E> {
    return (raw) => values.find((value) => value === raw.toUpperCase());
}

type Schema = Record<string, ParamParser<unknown>>;

export type Filters<S extends Schema> = {
    [K in keyof S]?: Exclude<ReturnType<S[K]>, undefined>;
};

// Filters live in the URL so refresh, back/forward and shared links keep them. Invalid values are dropped
// rather than sent to the server. `schema` must be a module-level constant.
export function useFilterParams<S extends Schema>(schema: S) {
    const [searchParams, setSearchParams] = useSearchParams();
    const search = searchParams.toString();

    const filters = React.useMemo(() => {
        const params = new URLSearchParams(search);
        const parsed: Record<string, unknown> = {};
        for (const [key, parse] of Object.entries(schema)) {
            const raw = params.get(key);
            const value = raw === null ? undefined : parse(raw);
            if (value !== undefined) {
                parsed[key] = value;
            }
        }
        return parsed as Filters<S>;
    }, [schema, search]);

    const setFilters = React.useCallback(
        (patch: Filters<S>) => {
            setSearchParams(
                (current) => {
                    const next = new URLSearchParams(current);
                    for (const [key, value] of Object.entries(patch)) {
                        if (value === undefined) {
                            next.delete(key);
                        } else {
                            next.set(key, String(value));
                        }
                    }
                    return next;
                },
                { replace: true },
            );
        },
        [setSearchParams],
    );

    return [filters, setFilters] as const;
}
