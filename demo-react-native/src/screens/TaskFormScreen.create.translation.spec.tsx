import {waitFor} from '@testing-library/react-native';
import i18n from 'i18next';

import {TaskFormScreen} from './TaskFormScreen';
import enMessages from '../locales/en/translation.json';
import esMessages from '../locales/es/translation.json';
import {renderWithProviders} from '@/test-utils/renderWithProviders';
import {trackTranslationCalls} from '@/test-utils/trackTranslationCalls';

const navigation = { goBack: jest.fn(), navigate: jest.fn() };
const createRoute = { key: 'CreateTask', name: 'CreateTask' as const, params: undefined };

describe('TaskFormScreen create translation', () => {
  it.each([
    { language: 'en', messages: enMessages },
    { language: 'es', messages: esMessages },
  ])('should have translations for create flow in $language', async ({ language, messages }) => {
    // Given
    const { calls } = trackTranslationCalls();
    await i18n.changeLanguage(language);

    // When
    renderWithProviders(
      <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
    );

    // Then
    await waitFor(() => {
      expect(calls).toHaveTranslations(messages as Record<string, unknown>);
    });
  });
});
