import {TaskPriority, TaskStatus} from '@/data/models/task';
import {PriorityChip, StatusChip} from './TaskChips';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

describe('TaskChips', () => {
  it.each([
    [TaskStatus.TODO, `status-tag-${TaskStatus.TODO}`, 'To Do'],
    [TaskStatus.IN_PROGRESS, `status-tag-${TaskStatus.IN_PROGRESS}`, 'In Progress'],
    [TaskStatus.DONE, `status-tag-${TaskStatus.DONE}`, 'Done'],
  ])('should render status chip when status is %s', (status, testId, label) => {
    // When
    const { getByTestId } = renderWithProviders(<StatusChip status={status} />);

    // Then
    expect(getByTestId(testId)).toHaveTextContent(label);
  });

  it.each([
    [TaskPriority.LOW, `priority-tag-${TaskPriority.LOW}`, 'Low'],
    [TaskPriority.MEDIUM, `priority-tag-${TaskPriority.MEDIUM}`, 'Medium'],
    [TaskPriority.HIGH, `priority-tag-${TaskPriority.HIGH}`, 'High'],
  ])('should render priority chip when priority is %s', (priority, testId, label) => {
    // When
    const { getByTestId } = renderWithProviders(<PriorityChip priority={priority} />);

    // Then
    expect(getByTestId(testId)).toHaveTextContent(label);
  });
});
