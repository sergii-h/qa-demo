import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'node:path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'happy-dom',
    setupFiles: './src/setupTests.ts',
    css: true,
    exclude: [
      'node_modules',
      'src/components_OLD/**',
      'src/pact_OLD/**',
    ],
    coverage: {
      provider: 'istanbul',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/setupTests.ts',
        '**/*.d.ts',
        '**/*.config.*',
        '**/mockData',
        'src/index.tsx',
        'src/reportWebVitals.ts',
        'src/components_OLD/**',
        'pact_OLD/**',
        'src/mocks/**',
        'FixJSDOMEnvironment.ts',
        '.storybook/**',
        'src/test-utils/**',
      ],
      thresholds: {
        lines: 90,
        functions: 90,
        branches: 90,
        statements: 90,
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
    },
  },
});

