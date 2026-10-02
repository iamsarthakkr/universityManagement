/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
    plugins: [react(), tailwindcss()],
    resolve: {
        alias: {
            '@': import.meta.dirname,
        },
    },
    server: {
        port: 3000,
        strictPort: true,
    },
    test: {
        environment: 'jsdom',
        setupFiles: ['./tests/setup.ts'],
        restoreMocks: true,
    },
});
