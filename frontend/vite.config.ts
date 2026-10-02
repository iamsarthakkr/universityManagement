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
        coverage: {
            provider: 'v8',
            include: [
                'components/**',
                'config/**',
                'hooks/**',
                'lib/**',
                'pages/**',
                'stores/**',
                'types/**',
                'router.tsx',
            ],
            exclude: ['components/ui/base/**', '**/*.test.{ts,tsx}', 'types/**/!(registration).ts'],
            reporter: ['text', 'html', 'lcov'],
            thresholds: {
                statements: 90,
                branches: 80,
                functions: 90,
                lines: 90,
            },
        },
    },
});
