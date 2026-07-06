import {fireEvent, RenderAPI, waitFor} from '@testing-library/react-native';

import AsyncStorage from '@react-native-async-storage/async-storage';

import {TaskListScreen} from '@/screens/TaskListScreen';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {ENGLISH, setLanguage} from '@/i18n';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

async function switchToSpanish(screen: RenderAPI) {
  fireEvent.press(screen.getByTestId('language-switcher'));
  await waitFor(() => {
    expect(screen.getByTestId('language-option-es')).toBeOnTheScreen();
  });
  fireEvent.press(screen.getByTestId('language-option-es'));
}

async function switchToEnglish(screen: RenderAPI) {
  fireEvent.press(screen.getByTestId('language-switcher'));
  await waitFor(() => {
    expect(screen.getByTestId('language-option-en')).toBeOnTheScreen();
  });
  fireEvent.press(screen.getByTestId('language-option-en'));
}

const navigation = {
  navigate: jest.fn(),
};

const route = {
  key: 'TaskList',
  name: 'TaskList' as const,
  params: undefined,
};

describe('LanguageSwitcher integration', () => {
  beforeEach(async () => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
    await AsyncStorage.clear();
    await setLanguage(ENGLISH);
  });

  describe('Language selection tests', () => {
    it('should render language switcher with EN and ES options', async () => {
    // Given
    mockFetchResponse({
      '/v1/tasks': {
        GET: { body: [] },
      },
    });
    const screen = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    await waitFor(() => {
      expect(screen.getByTestId('language-switcher')).toBeVisible();
    });

    // When
    fireEvent.press(screen.getByTestId('language-switcher'));

    // Then
    await waitFor(() => {
      expect(screen.getByTestId('language-option-en')).toBeOnTheScreen();
      expect(screen.getByTestId('language-option-es')).toBeOnTheScreen();
    });
    });

    it('should change current language when user selects another language option', async () => {
    // Given
    const task = { id: '1', title: 'Task One', status: TaskStatus.TODO, priority: TaskPriority.MEDIUM };
    mockFetchResponse({
      '/v1/tasks': {
        GET: { body: [task] },
      },
    });
    const screen = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );
    await waitFor(() => {
      expect(screen.getByTestId(`task-title-${'1'}`)).toBeVisible();
    });
    expect(screen.getByTestId('page-title')).toHaveTextContent('Tasks');

    // When
    await switchToSpanish(screen);

    // Then
    await waitFor(() => {
      expect(screen.getByTestId('page-title')).toHaveTextContent('Tareas');
    });

    // When
    await switchToEnglish(screen);

    // Then
    await waitFor(() => {
      expect(screen.getByTestId('page-title')).toHaveTextContent('Tasks');
    });
  });
  });
});
