import {waitFor} from '@testing-library/react-native';
import i18n from 'i18next';

import {TaskPriority, TaskStatus} from '@/data/models/task';
import {TaskListScreen} from './TaskListScreen';
import enMessages from '../locales/en/translation.json';
import esMessages from '../locales/es/translation.json';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {renderWithProviders} from '@/test-utils/renderWithProviders';
import {trackTranslationCalls} from '@/test-utils/trackTranslationCalls';

const navigation = { navigate: jest.fn() };
const route = { key: 'TaskList', name: 'TaskList' as const, params: undefined };

describe('TaskListScreen translation', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  it.each([
    { language: 'en', messages: enMessages },
    { language: 'es', messages: esMessages },
  ])('should have translations for task list in $language', async ({ language, messages }) => {
    // Given
    const { calls } = trackTranslationCalls();
    await i18n.changeLanguage(language);
    const task = {
      id: 'task-translation',
      title: 'Task title',
      status: TaskStatus.TODO,
      priority: TaskPriority.MEDIUM,
    };
    mockFetchResponse({
      '/v1/tasks': {
        GET: { body: [task] },
      },
    });

    // When
    renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    // Then
    await waitFor(() => {
      expect(calls).toHaveTranslations(messages as Record<string, unknown>);
    });
  });
});
