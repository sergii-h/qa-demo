import {fireEvent, RenderAPI, waitFor} from '@testing-library/react-native';

import AsyncStorage from '@react-native-async-storage/async-storage';

import {TaskListScreen} from '@/screens/TaskListScreen';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {ENGLISH, setLanguage} from '@/i18n';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {TestTags} from '@/testTags';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

async function switchToSpanish(screen: RenderAPI) {
  fireEvent.press(screen.getByTestId(TestTags.LANGUAGE_SWITCHER));
  await waitFor(() => {
    expect(screen.getByTestId(TestTags.LANGUAGE_OPTION_ES)).toBeOnTheScreen();
  });
  fireEvent.press(screen.getByTestId(TestTags.LANGUAGE_OPTION_ES));
}

async function switchToEnglish(screen: RenderAPI) {
  fireEvent.press(screen.getByTestId(TestTags.LANGUAGE_SWITCHER));
  await waitFor(() => {
    expect(screen.getByTestId(TestTags.LANGUAGE_OPTION_EN)).toBeOnTheScreen();
  });
  fireEvent.press(screen.getByTestId(TestTags.LANGUAGE_OPTION_EN));
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
      expect(screen.getByTestId(TestTags.LANGUAGE_SWITCHER)).toBeVisible();
    });

    // When
    fireEvent.press(screen.getByTestId(TestTags.LANGUAGE_SWITCHER));

    // Then
    await waitFor(() => {
      expect(screen.getByTestId(TestTags.LANGUAGE_OPTION_EN)).toBeOnTheScreen();
      expect(screen.getByTestId(TestTags.LANGUAGE_OPTION_ES)).toBeOnTheScreen();
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
      expect(screen.getByTestId(TestTags.taskTitle('1'))).toBeVisible();
    });
    expect(screen.getByTestId(TestTags.PAGE_TITLE)).toHaveTextContent('Tasks');

    // When
    await switchToSpanish(screen);

    // Then
    await waitFor(() => {
      expect(screen.getByTestId(TestTags.PAGE_TITLE)).toHaveTextContent('Tareas');
    });

    // When
    await switchToEnglish(screen);

    // Then
    await waitFor(() => {
      expect(screen.getByTestId(TestTags.PAGE_TITLE)).toHaveTextContent('Tasks');
    });
  });
  });
});
