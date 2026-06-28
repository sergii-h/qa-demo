import {waitFor} from '@testing-library/react-native';
import i18n from 'i18next';

import {TaskPriority, TaskStatus} from '@/data/models/task';
import {TaskDetailScreen} from './TaskDetailScreen';
import enMessages from '../locales/en/translation.json';
import esMessages from '../locales/es/translation.json';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {TestTags} from '@/testTags';
import {renderWithProviders} from '@/test-utils/renderWithProviders';
import {trackTranslationCalls} from '@/test-utils/trackTranslationCalls';

const navigation = { goBack: jest.fn() };

function detailRoute(taskId: string) {
  return { key: 'TaskDetail', name: 'TaskDetail' as const, params: { taskId } };
}

describe('TaskDetailScreen translation', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  it.each([
    { language: 'en', messages: enMessages },
    { language: 'es', messages: esMessages },
  ])('should have translations for detail view in $language', async ({ language, messages }) => {
    // Given
    const { calls } = trackTranslationCalls();
    await i18n.changeLanguage(language);
    const task = {
      id: 'task-translation',
      title: 'Task title',
      description: 'Task description',
      status: TaskStatus.IN_PROGRESS,
      priority: TaskPriority.HIGH,
      createdDate: '2024-01-15T10:00:00.000Z',
      updatedDate: '2024-01-16T12:00:00.000Z',
    };
    mockFetchResponse({
      [`/v1/tasks/${task.id}`]: {
        GET: { body: task },
      },
      [`/v1/tasks/isValid/${task.id}`]: {
        GET: { body: true },
      },
    });

    // When
    const screen = renderWithProviders(
      <TaskDetailScreen navigation={navigation as never} route={detailRoute(task.id) as never} />,
    );
    await waitFor(() => {
      expect(screen.getByTestId(TestTags.DESCRIPTION)).toBeTruthy();
    });

    // Then
    expect(calls).toHaveTranslations(messages as Record<string, unknown>);
  });
});
