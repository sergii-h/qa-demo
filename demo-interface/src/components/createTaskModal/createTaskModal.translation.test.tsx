import { render, waitFor } from '@testing-library/react';
import { CreateTaskModal } from './createTaskModal';
import enMessages from '../../locales/en/translation.json';
import esMessages from '../../locales/es/translation.json';

const translationCalls: Array<string> = [];
let resolvedLanguage = 'en';

vi.mock('react-i18next', async () => {
  const actual = await vi.importActual('react-i18next');
  return {
    ...actual,
    useTranslation: () => ({
      t: (key: string) => {
        translationCalls.push(key);
        return key;
      },
      i18n: { resolvedLanguage },
    }),
  };
});


describe('CreateTaskModal translation', () => {
  beforeEach(() => {
    translationCalls.length = 0;
  });

  it.each([
    { language: 'en', messages: enMessages },
    { language: 'es', messages: esMessages },
  ])('should have translations for create task modal in $language', async ({ language, messages }) => {
    // given
    resolvedLanguage = language;
    render(<CreateTaskModal onClose={() => {}} onSave={() => {}} />);
      
    // then
    await waitFor(() => {
      expect(translationCalls).toHaveTranslations(messages as Record<string, unknown>);
    });
  });
});
