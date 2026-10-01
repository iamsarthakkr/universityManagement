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
        // Backend CORS only allows http://localhost:3000 (server SecurityConfig)
        port: 3000,
        strictPort: true,
    },
});
