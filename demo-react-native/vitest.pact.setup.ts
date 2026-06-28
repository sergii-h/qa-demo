import { vi } from 'vitest';

vi.mock('react-native', () => ({
  Platform: { OS: 'ios' },
}));

vi.mock('expo-constants', () => ({
  default: {
    expoConfig: {
      extra: {},
    },
  },
}));
