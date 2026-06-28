import {fireEvent} from '@testing-library/react-native';

import {TaskDetailScreen} from './TaskDetailScreen';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {useTaskDetail} from '@/hooks/useTaskDetail';
import {mockTask} from '@/test-utils/taskFixtures';
import {TestTags} from '@/testTags';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

jest.mock('../hooks/useTaskDetail');

const navigation = {
  goBack: jest.fn(),
};

const route = {
  key: 'TaskDetail',
  name: 'TaskDetail' as const,
  params: { taskId: 'task-1' },
};

function renderTaskDetailScreen() {
  const screen = renderWithProviders(
    <TaskDetailScreen navigation={navigation as never} route={route as never} />,
  );
  expect(useTaskDetail).toHaveBeenCalledWith(route.params.taskId);
  return screen;
}

describe('TaskDetailScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.mocked(useTaskDetail).mockReturnValue({
      isLoading: false,
      task: mockTask,
      isValid: true,
      errorMessage: null,
    });
  });

  it('should show loading spinner when loading', () => {
    // Given
    jest.mocked(useTaskDetail).mockReturnValue({
      isLoading: true,
      task: null,
      isValid: false,
      errorMessage: null,
    });

    // When
    const { getByTestId } = renderTaskDetailScreen();

    // Then
    expect(getByTestId(TestTags.LOADING_SPINNER)).toBeVisible();
  });

  it('should render empty content when task is missing without error', () => {
    // Given
    jest.mocked(useTaskDetail).mockReturnValue({
      isLoading: false,
      task: null,
      isValid: false,
      errorMessage: null,
    });

    // When
    const { queryByTestId } = renderTaskDetailScreen();

    // Then
    expect(queryByTestId(TestTags.LOAD_ERROR)).toBeNull();
    expect(queryByTestId(TestTags.DESCRIPTION)).toBeNull();
  });

  it('should show load error when task load fails', () => {
    // Given
    jest.mocked(useTaskDetail).mockReturnValue({
      isLoading: false,
      task: null,
      isValid: false,
      errorMessage: 'Task not found',
    });

    // When
    const { getByTestId } = renderTaskDetailScreen();

    // Then
    expect(getByTestId(TestTags.LOAD_ERROR)).toHaveTextContent('Task not found');
  });

  it('should render task details with valid status', () => {
    // Given
    jest.mocked(useTaskDetail).mockReturnValue({
      isLoading: false,
      task: {
        ...mockTask,
        description: '  ',
        createdDate: null,
        updatedDate: 'invalid-date',
      },
      isValid: true,
      errorMessage: null,
    });

    // When
    const { getByTestId } = renderTaskDetailScreen();

    // Then
    expect(getByTestId(TestTags.MODAL_TITLE)).toHaveTextContent(mockTask.title);
    expect(getByTestId(TestTags.DESCRIPTION)).toHaveTextContent('No description');
    expect(getByTestId(TestTags.CREATED_DATE)).toHaveTextContent('N/A');
    expect(getByTestId(TestTags.UPDATED_DATE)).toHaveTextContent('invalid-date');
    expect(getByTestId(TestTags.VALID)).toHaveTextContent('✓');
    expect(getByTestId(TestTags.DETAIL_VALIDATED_LABEL)).toHaveTextContent('Validated');
    expect(getByTestId(TestTags.statusTag(TaskStatus.TODO))).toHaveTextContent('To Do');
    expect(getByTestId(TestTags.priorityTag(TaskPriority.MEDIUM))).toHaveTextContent('Medium');
  });

  it('should render not valid status when validation fails', () => {
    // Given
    jest.mocked(useTaskDetail).mockReturnValue({
      isLoading: false,
      task: {
        ...mockTask,
        description: null,
        status: TaskStatus.DONE,
        priority: TaskPriority.HIGH,
      },
      isValid: false,
      errorMessage: null,
    });

    // When
    const { getByTestId } = renderTaskDetailScreen();

    // Then
    expect(getByTestId(TestTags.NOT_VALID)).toHaveTextContent('✕');
    expect(getByTestId(TestTags.statusTag(TaskStatus.DONE))).toHaveTextContent('Done');
    expect(getByTestId(TestTags.priorityTag(TaskPriority.HIGH))).toHaveTextContent('High');
  });

  it('should navigate back when back button pressed', () => {
    // Given
    const { getByTestId } = renderTaskDetailScreen();

    // When
    fireEvent.press(getByTestId(TestTags.CLOSE_BUTTON));

    // Then
    expect(navigation.goBack).toHaveBeenCalled();
  });
});
