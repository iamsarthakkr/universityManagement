type QueryValue = string | number | boolean | null | undefined;

// Appends only the params that are set, so optional filters never reach the server as `?status=undefined`.
export function withQuery(path: string, params: Record<string, QueryValue> = {}) {
    const search = new URLSearchParams();
    for (const [key, value] of Object.entries(params)) {
        if (value !== undefined && value !== null && value !== '') {
            search.append(key, String(value));
        }
    }

    const query = search.toString();
    return query ? `${path}?${query}` : path;
}
