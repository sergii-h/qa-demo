import {TaskPriority, TaskStatus} from '@/data/models/task';
import {TestTags} from '@/testTags';
import {PriorityChip, StatusChip} from './TaskChips';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

describe('TaskChips', () => {
  it.each([
    [TaskStatus.TODO, TestTags.statusTag(TaskStatus.TODO), 'To Do'],
    [TaskStatus.IN_PROGRESS, TestTags.statusTag(TaskStatus.IN_PROGRESS), 'In Progress'],
    [TaskStatus.DONE, TestTags.statusTag(TaskStatus.DONE), 'Done'],
  ])('should render status chip when status is %s', (status, testId, label) => {
    // When
    const { getByTestId } = renderWithProviders(<StatusChip status={status} />);

    // Then
    expect(getByTestId(testId)).toHaveTextContent(label);
  });

  it.each([
    [TaskPriority.LOW, TestTags.priorityTag(TaskPriority.LOW), 'Low'],
    [TaskPriority.MEDIUM, TestTags.priorityTag(TaskPriority.MEDIUM), 'Medium'],
    [TaskPriority.HIGH, TestTags.priorityTag(TaskPriority.HIGH), 'High'],
  ])('should render priority chip when priority is %s', (priority, testId, label) => {
    // When
    const { getByTestId } = renderWithProviders(<PriorityChip priority={priority} />);

    // Then
    expect(getByTestId(testId)).toHaveTextContent(label);
  });
});
