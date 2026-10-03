import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeEach, vi } from 'vitest';

import { queryClient } from '@/lib/query';
import { setUnauthorizedHandler } from '@/lib/http';
import { useAppStore } from '@/stores/appStore';

const prefersDark = { matches: false };

export function setPrefersDark(matches: boolean) {
    prefersDark.matches = matches;
}

Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: (query: string) => ({
        get matches() {
            return query.includes('prefers-color-scheme: dark') ? prefersDark.matches : false;
        },
        media: query,
        onchange: null,
        addEventListener: () => {},
        removeEventListener: () => {},
        addListener: () => {},
        removeListener: () => {},
        dispatchEvent: () => false,
    }),
});

class ResizeObserverStub {
    observe() {}
    unobserve() {}
    disconnect() {}
}
window.ResizeObserver = ResizeObserverStub;
Element.prototype.scrollIntoView = () => {};
Element.prototype.hasPointerCapture = () => false;
Element.prototype.releasePointerCapture = () => {};

const initialAppState = useAppStore.getState();

beforeEach(() => {
    localStorage.clear();
    document.documentElement.className = '';
    setPrefersDark(false);
    useAppStore.setState(initialAppState, true);
    setUnauthorizedHandler(null);
});

afterEach(() => {
    cleanup();
    queryClient.clear();
    vi.unstubAllGlobals();
});
