import {fireEvent, waitFor} from '@testing-library/react-native';
import i18n from 'i18next';

import {LanguageSwitcher} from './LanguageSwitcher';
import * as appI18n from '../i18n';
import {TestTags} from '@/testTags';
import {dismissPaperMenu, paperMenuMock} from '@/test-utils/paperMenuSpy';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

jest.mock('react-native-paper', () =>
  require('../test-utils/paperMenuSpy').paperMenuModuleMock(),
);

describe('LanguageSwitcher', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render language switcher', () => {
    // When
    const { getByTestId } = renderWithProviders(<LanguageSwitcher />);

    // Then
    expect(getByTestId(TestTags.LANGUAGE_SWITCHER)).toHaveTextContent('EN');
  });

  it('should call setLanguage with english when EN selected', async () => {
    // Given
    const setLanguageSpy = jest.spyOn(appI18n, 'setLanguage').mockResolvedValue();
    const { getByTestId } = renderWithProviders(<LanguageSwitcher />);

    // When
    fireEvent.press(getByTestId(TestTags.LANGUAGE_SWITCHER));
    await waitFor(() => {
      expect(getByTestId(TestTags.LANGUAGE_OPTION_EN)).toBeOnTheScreen();
    });
    fireEvent.press(getByTestId(TestTags.LANGUAGE_OPTION_EN));

    // Then
    expect(setLanguageSpy).toHaveBeenCalledWith(appI18n.ENGLISH);
  });

  it('should call setLanguage with spanish when ES selected', async () => {
    // Given
    const setLanguageSpy = jest.spyOn(appI18n, 'setLanguage').mockResolvedValue();
    const { getByTestId } = renderWithProviders(<LanguageSwitcher />);

    // When
    fireEvent.press(getByTestId(TestTags.LANGUAGE_SWITCHER));
    await waitFor(() => {
      expect(getByTestId(TestTags.LANGUAGE_OPTION_ES)).toBeOnTheScreen();
    });
    fireEvent.press(getByTestId(TestTags.LANGUAGE_OPTION_ES));

    // Then
    expect(setLanguageSpy).toHaveBeenCalledWith(appI18n.SPANISH);
  });

  it('should show spanish label when current language is spanish', async () => {
    // Given
    await i18n.changeLanguage(appI18n.SPANISH);

    // When
    const { getByTestId } = renderWithProviders(<LanguageSwitcher />);

    // Then
    expect(getByTestId(TestTags.LANGUAGE_SWITCHER)).toHaveTextContent('ES');
  });

  it('should close menu when dismissed', async () => {
    // Given
    const { getByTestId } = renderWithProviders(<LanguageSwitcher />);
    fireEvent.press(getByTestId(TestTags.LANGUAGE_SWITCHER));
    expect(paperMenuMock.mock.calls.at(-1)?.[0].visible).toBe(true);

    // When
    dismissPaperMenu();

    // Then
    await waitFor(() => {
      expect(paperMenuMock.mock.calls.at(-1)?.[0].visible).toBe(false);
    });
  });
});
