import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Localization from 'expo-localization';
import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';

import en from '../locales/en/translation.json';
import es from '../locales/es/translation.json';

export const ENGLISH = 'en';
export const SPANISH = 'es';

const LOCALE_STORAGE_KEY = 'demo_locale';

function defaultLanguageTag(): string {
  const deviceLanguage = Localization.getLocales()[0]?.languageCode ?? ENGLISH;
  return deviceLanguage.startsWith(SPANISH) ? SPANISH : ENGLISH;
}

export { defaultLanguageTag };

export async function initI18n(): Promise<void> {
  const storedLanguage =
    (await AsyncStorage.getItem(LOCALE_STORAGE_KEY)) ?? defaultLanguageTag();

  await i18n.use(initReactI18next).init({
    resources: {
      en: { translation: en },
      es: { translation: es },
    },
    lng: storedLanguage,
    fallbackLng: ENGLISH,
    interpolation: {
      escapeValue: false,
    },
  });
}

export async function setLanguage(languageTag: string): Promise<void> {
  if (i18n.language === languageTag) {
    return;
  }
  await AsyncStorage.setItem(LOCALE_STORAGE_KEY, languageTag);
  await i18n.changeLanguage(languageTag);
}

export { i18n };
