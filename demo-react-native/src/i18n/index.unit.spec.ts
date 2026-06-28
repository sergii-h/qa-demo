import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Localization from 'expo-localization';

import {defaultLanguageTag, ENGLISH, i18n, initI18n, setLanguage, SPANISH} from './index';

describe('i18n', () => {
  beforeEach(async () => {
    await AsyncStorage.clear();
    jest.mocked(Localization.getLocales).mockReturnValue([
      { languageCode: 'en' } as Localization.Locale,
    ]);
  });

  it('should default to spanish when device locale is spanish', () => {
    jest.mocked(Localization.getLocales).mockReturnValue([
      { languageCode: 'es' } as Localization.Locale,
    ]);

    expect(defaultLanguageTag()).toBe(SPANISH);
  });

  it('should default to english when device locale is not spanish', () => {
    jest.mocked(Localization.getLocales).mockReturnValue([
      { languageCode: 'en' } as Localization.Locale,
    ]);

    expect(defaultLanguageTag()).toBe(ENGLISH);
  });

  it('should initialize with stored language', async () => {
    await AsyncStorage.setItem('demo_locale', SPANISH);
    await initI18n();

    expect(i18n.language).toBe(SPANISH);
  });

  it('should set language and persist preference', async () => {
    await initI18n();
    await setLanguage(SPANISH);

    expect(i18n.language).toBe(SPANISH);
    expect(await AsyncStorage.getItem('demo_locale')).toBe(SPANISH);
  });

  it('should skip setLanguage when language is unchanged', async () => {
    await initI18n();
    await setLanguage(ENGLISH);
    jest.mocked(AsyncStorage.setItem).mockClear();

    await setLanguage(ENGLISH);

    expect(AsyncStorage.setItem).not.toHaveBeenCalled();
  });
});
