import js from '@eslint/js';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import prettier from 'eslint-config-prettier';

export default tseslint.config(
    { ignores: ['node_modules/', 'dist/'] },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    react.configs.flat.recommended,
    react.configs.flat['jsx-runtime'],
    prettier,
    {
        languageOptions: {
            globals: { ...globals.browser },
        },
        plugins: { 'react-hooks': reactHooks },
        // Classic hooks rules only, matching what next/core-web-vitals enforced.
        // The v7 React Compiler rules (set-state-in-effect etc.) are a separate cleanup.
        rules: {
            'react-hooks/rules-of-hooks': 'error',
            'react-hooks/exhaustive-deps': 'warn',
        },
        settings: {
            react: { version: 'detect' },
        },
    },
    {
        files: ['vite.config.ts', 'eslint.config.mjs', '*.cjs'],
        languageOptions: {
            globals: { ...globals.node },
        },
    },
);
