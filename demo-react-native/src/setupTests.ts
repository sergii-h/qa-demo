import '@testing-library/react-native/extend-expect';
import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';

import en from './locales/en/translation.json';
import es from './locales/es/translation.json';
import {translationMatchers} from './test-utils/translationMatchers';
import {releaseTranslationSpy} from './test-utils/trackTranslationCalls';

expect.extend(translationMatchers);

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

if (!i18n.isInitialized) {
  i18n.use(initReactI18next).init({
    lng: 'en',
    fallbackLng: 'en',
    resources: {
      en: { translation: en },
      es: { translation: es },
    },
    interpolation: {
      escapeValue: false,
    },
  });
}

beforeEach(async () => {
  if (i18n.language !== 'en') {
    await i18n.changeLanguage('en');
  }
});

afterEach(() => {
  releaseTranslationSpy();
  jest.restoreAllMocks();
});
