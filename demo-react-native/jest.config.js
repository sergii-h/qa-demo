module.exports = {
  preset: 'jest-expo',
  setupFiles: ['<rootDir>/src/jest.setup.mocks.ts'],
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
  transformIgnorePatterns: [
    'node_modules/(?!((jest-)?react-native|@react-native(-community)?)|expo(nent)?|@expo(nent)?/.*|@expo-google-fonts/.*|react-navigation|@react-navigation/.*|react-native-paper|react-native-safe-area-context|react-native-screens|react-native-gesture-handler)',
  ],
  testMatch: [
    '**/*.unit.spec.(ts|tsx)',
    '**/*.integration.spec.(ts|tsx)',
    '**/*.translation.spec.(ts|tsx)',
  ],
  testPathIgnorePatterns: ['/node_modules/', '/src/test/pact/'],
  collectCoverageFrom: [
    'App.tsx',
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/index.ts',
    '!src/navigation/types.ts',
    '!src/testTags.ts',
    '!src/theme/**',
    '!src/locales/**',
    '!src/test/pact/**',
    '!src/data/models/**',
    '!src/test-utils/**',
  ],
  coverageProvider: 'babel',
  coverageReporters: ['text', 'json', 'html', 'lcov'],
  coverageThreshold: {
    global: {
      branches: 90,
      functions: 90,
      lines: 90,
      statements: 90,
    },
  },
};
