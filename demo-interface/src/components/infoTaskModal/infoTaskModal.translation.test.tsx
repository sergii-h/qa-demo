import { render, waitFor } from '@testing-library/react';
import InfoTaskModal from './infoTaskModal';
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

describe('InfoTaskModal translation', () => {
  beforeEach(() => {
    translationCalls.length = 0;
    vi.spyOn(services, 'getTask').mockResolvedValue({
      id: 'task-translation',
      title: 'Task title',
      description: 'Task description',
      status: TaskStatus.IN_PROGRESS,
      priority: TaskPriority.HIGH,
      createdDate: '2024-01-15T10:00:00.000Z',
      updatedDate: '2024-01-16T12:00:00.000Z',
    });
    vi.spyOn(services, 'getIsValid').mockResolvedValue(true);
  });

  it.each([
    { language: 'en', messages: enMessages },
    { language: 'es', messages: esMessages },
  ])('should have translations for info task modal in $language', async ({ language, messages }) => {
    // given
    resolvedLanguage = language;
    render(<InfoTaskModal taskId="task-translation" onClose={() => {}} />);

    // then
    await waitFor(() => {
      expect(translationCalls).toHaveTranslations(messages as Record<string, unknown>);
    });
  });
});
