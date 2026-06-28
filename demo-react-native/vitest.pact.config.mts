import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    globals: true,
    environment: 'node',
    include: ['src/test/pact/**/*.pact.test.ts'],
    setupFiles: ['./vitest.pact.setup.ts'],
  },
});
