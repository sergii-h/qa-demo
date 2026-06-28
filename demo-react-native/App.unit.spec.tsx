import { render, waitFor } from '@testing-library/react-native';
import { PaperProvider } from 'react-native-paper';

import App from './App';
import { AppNavigator } from '@/navigation/AppNavigator';
import { initI18n } from '@/i18n';
import { themeColors } from '@/theme/colors';

jest.mock('./src/i18n', () => ({
  initI18n: jest.fn(),
}));

jest.mock('./src/navigation/AppNavigator', () => ({
  AppNavigator: jest.fn(() => null),
}));

jest.mock('react-native-paper', () => {
  const actual = jest.requireActual('react-native-paper');
  return {
    ...actual,
    PaperProvider: jest.fn(({ children }: { children: React.ReactNode }) => children),
  };
});

describe('App', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should not render app navigator while i18n initializes', () => {
    // Given
    jest.mocked(initI18n).mockImplementation(() => new Promise(() => undefined));

    // When
    render(<App />);

    // Then
    expect(initI18n).toHaveBeenCalledTimes(1);
    expect(AppNavigator).not.toHaveBeenCalled();
  });

  it('should render app navigator when i18n is ready', async () => {
    // Given
    jest.mocked(initI18n).mockResolvedValue();

    // When
    render(<App />);

    // Then
    await waitFor(() => {
      expect(initI18n).toHaveBeenCalledTimes(1);
      expect(AppNavigator).toHaveBeenCalledTimes(1);
      expect(PaperProvider).toHaveBeenCalled();
    });

    const { theme, settings } = jest.mocked(PaperProvider).mock.calls[0][0];
    expect(theme?.colors?.primary).toBe(themeColors.primary);
    expect(
      settings?.icon?.({ name: 'check', size: 24, color: '#000000' }),
    ).toBeTruthy();
  });
});
