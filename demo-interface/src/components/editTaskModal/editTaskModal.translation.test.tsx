import { render, waitFor } from '@testing-library/react';
import EditTaskModal from './editTaskModal';
import * as services from '../../services';
import { TaskPriority, TaskStatus } from '../../interfaces/ITask';
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

describe('EditTaskModal translation', () => {
  beforeEach(() => {
    translationCalls.length = 0;
    vi.spyOn(services, 'getTask').mockResolvedValue({
      id: 'task-translation',
      title: 'Task title',
      description: 'Task description',
      status: TaskStatus.TODO,
      priority: TaskPriority.MEDIUM,
    });
  });

  it.each([
    { language: 'en', messages: enMessages },
    { language: 'es', messages: esMessages },
  ])('should have translations for edit task modal in $language', async ({ language, messages }) => {
    // given
    resolvedLanguage = language;
    render(<EditTaskModal taskId="task-translation" onClose={() => {}} onSave={() => {}} />);

    // then
    await waitFor(() => {
      expect(translationCalls).toHaveTranslations(messages as Record<string, unknown>);
    });
  });
});
