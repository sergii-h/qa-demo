import {waitFor} from '@testing-library/react-native';
import i18n from 'i18next';

import {TaskPriority, TaskStatus} from '@/data/models/task';
import {TaskFormScreen} from './TaskFormScreen';
import enMessages from '../locales/en/translation.json';
import esMessages from '../locales/es/translation.json';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {TestTags} from '@/testTags';
import {renderWithProviders} from '@/test-utils/renderWithProviders';
import {trackTranslationCalls} from '@/test-utils/trackTranslationCalls';

const navigation = { goBack: jest.fn(), navigate: jest.fn() };

function editRoute(taskId: string) {
  return { key: 'EditTask', name: 'EditTask' as const, params: { taskId } };
}

describe('TaskFormScreen edit translation', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  it.each([
    { language: 'en', messages: enMessages },
    { language: 'es', messages: esMessages },
  ])('should have translations for edit flow in $language', async ({ language, messages }) => {
    // Given
    const { calls } = trackTranslationCalls();
    await i18n.changeLanguage(language);
    const task = {
      id: 'task-translation',
      title: 'Task title',
      description: 'Task description',
      status: TaskStatus.TODO,
      priority: TaskPriority.MEDIUM,
    };
    mockFetchResponse({
      [`/v1/tasks/${task.id}`]: {
        GET: { body: task },
      },
    });

    // When
    const screen = renderWithProviders(
      <TaskFormScreen navigation={navigation as never} route={editRoute(task.id) as never} />,
    );
    await waitFor(() => {
      expect(screen.getByTestId(TestTags.EDIT_TASK_TITLE_INPUT)).toBeTruthy();
    });

    // Then
    expect(calls).toHaveTranslations(messages as Record<string, unknown>);
  });
});
