import {ReactNode} from 'react';
import {I18nextProvider} from 'react-i18next';

import i18n from '../i18n';

export function I18nWrapper({ children }: { children: ReactNode }) {
  return <I18nextProvider i18n={i18n}>{children}</I18nextProvider>;
}

export function createI18nWrapper() {
  return I18nWrapper;
}
