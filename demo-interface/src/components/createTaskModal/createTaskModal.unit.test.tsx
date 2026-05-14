import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CreateTaskModal from './createTaskModal';
import * as services from '../../services';
import { TaskStatus, TaskPriority } from '../../interfaces/ITask';

describe('CreateTaskModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSave = vi.fn();
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe('Rendering', () => {
    it('should render with default values', () => {
      // when
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      // then
      expect(screen.getByTestId('modal-title')).toBeVisible();
      expect(screen.getByLabelText(/title/i)).toHaveValue('');
      expect(screen.getByLabelText(/description/i)).toHaveValue('');
      expect(screen.getByTestId('create-button')).toBeDisabled();
      expect(screen.getByTestId('close-button')).toBeVisible();
    });

    it('should show loading spinner when isLoading prop is true', () => {
      // when
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} isLoading={true} />);

      // then
      expect(screen.getByTestId('loading-spinner')).toBeVisible();
      expect(screen.queryByLabelText(/title/i)).not.toBeInTheDocument();
    });
  });

  describe('Title Validation', () => {
    it('should disable Create button when title is empty', async () => {
      // when
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      // then
      expect(screen.getByTestId('create-button')).toBeDisabled();
    });

    it('should show error when title exceeds 100 characters', async () => {
      // given
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);
      await user.type(screen.getByTestId('create-task-title-input'), 'a'.repeat(101));
      
      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toHaveTextContent('Title must not exceed 100 characters');
      });
    });

    it('should clear error when user starts typing', async () => {
      // given
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      const titleInput = screen.getByTestId('create-task-title-input');
      await user.type(titleInput, 'a'.repeat(101));
      await user.click(screen.getByTestId('create-button'));

      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toBeVisible();
      });

      await user.clear(titleInput);

      // when
      await user.type(titleInput, 'Valid title');

      // then
      expect(screen.queryByTestId('title-error')).not.toBeInTheDocument();
    });

    it('should enable Create button when title has value', async () => {
      // given
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      const createButton = screen.getByTestId('create-button');
      expect(createButton).toBeDisabled();

      // when
      await user.type(screen.getByTestId('create-task-title-input'), 'Valid task title');

      // then
      expect(createButton).toBeEnabled();
    });
  });

  describe('Task Creation', () => {
    it('should call createTask service with correct data', async () => {
      // given
      const createTaskSpy = vi.spyOn(services, 'createTask').mockResolvedValue({
        id: '123',
        title: 'Test Task',
        description: 'Test Description',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      });

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');
      await user.type(screen.getByLabelText(/description/i), 'Test Description');

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(createTaskSpy).toHaveBeenCalledWith({
          title: 'Test Task',
          description: 'Test Description',
          status: TaskStatus.TODO,
          priority: TaskPriority.MEDIUM,
        });
      });
    });

    it('should call onSave and onClose callbacks after successful creation', async () => {
      // given
      vi.spyOn(services, 'createTask').mockResolvedValue({
        id: '123',
        title: 'Test Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      });

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledTimes(1);
        expect(mockOnClose).toHaveBeenCalledTimes(1);
      });
    });

    it('should disable Create button while request is in progress', async () => {
      // given
      let resolveCreateTask: ((value: {
        id: string;
        title: string;
        status: TaskStatus;
        priority: TaskPriority;
      }) => void) | undefined;

      vi.spyOn(services, 'createTask').mockImplementation(() =>
        new Promise((resolve) => {
          resolveCreateTask = resolve;
        })
      );

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);
      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      expect(screen.getByTestId('create-button')).toBeDisabled();

      resolveCreateTask?.({
        id: '123',
        title: 'Test Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      });

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledTimes(1);
        expect(mockOnClose).toHaveBeenCalledTimes(1);
      });
    });

    it('should call createTask only once when submit is triggered twice before rerender', async () => {
      // given
      let resolveCreateTask: ((value: {
        id: string;
        title: string;
        status: TaskStatus;
        priority: TaskPriority;
      }) => void) | undefined;

      const createTaskSpy = vi.spyOn(services, 'createTask').mockImplementation(() =>
        new Promise((resolve) => {
          resolveCreateTask = resolve;
        })
      );

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);
      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');

      // when
      const createButton = screen.getByTestId('create-button');
      createButton.dispatchEvent(new MouseEvent('click', { bubbles: true }));
      createButton.dispatchEvent(new MouseEvent('click', { bubbles: true }));

      // then
      expect(createTaskSpy).toHaveBeenCalledTimes(1);

      resolveCreateTask?.({
        id: '123',
        title: 'Test Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      });

      await waitFor(() => {
        expect(mockOnSave).toHaveBeenCalledTimes(1);
        expect(mockOnClose).toHaveBeenCalledTimes(1);
      });
    });

  });

  describe('Error Handling', () => {
    it('should show error when task with duplicate title already exists', async () => {
      // given
      vi.spyOn(services, 'createTask').mockRejectedValue(
        new Error('Task with title \'Test Task\' already exists.')
      );

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toHaveTextContent('Task with this title already exists');
      });

      expect(mockOnSave).not.toHaveBeenCalled();
      expect(mockOnClose).not.toHaveBeenCalled();
    });

    it('should show generic error when creation fails for unknown reason', async () => {
      // given
      vi.spyOn(services, 'createTask').mockRejectedValue(new Error('Network error'));

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toHaveTextContent('Failed to create task. Please try again.');
      });

      expect(mockOnSave).not.toHaveBeenCalled();
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe('User Interactions', () => {
    it('should call onClose when Close button is clicked', async () => {
      // given
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      // when
      await user.click(screen.getByTestId('close-button'));

      // then
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should update description when user types', async () => {
      // given
      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);
      const descriptionInput = screen.getByLabelText(/description/i);

      // when
      await user.type(descriptionInput, 'Test description');

      // then
      expect(descriptionInput).toHaveValue('Test description');
    });

    it('should send selected status when dropdown changes', async () => {
      // given
      const createTaskSpy = vi.spyOn(services, 'createTask').mockResolvedValue({
        id: '123',
        title: 'Test',
        status: TaskStatus.DONE,
        priority: TaskPriority.MEDIUM,
      });

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');
      await user.click(screen.getByTestId('status-dropdown'));

      // when
      await user.click(screen.getByTestId(`status-dropdown-option-${TaskStatus.DONE}`));
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(createTaskSpy).toHaveBeenCalledWith(
          expect.objectContaining({ status: TaskStatus.DONE })
        );
      });
    });

    it('should send selected priority when dropdown changes', async () => {
      // given
      const createTaskSpy = vi.spyOn(services, 'createTask').mockResolvedValue({
        id: '123',
        title: 'Test',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      });

      render(<CreateTaskModal onClose={mockOnClose} onSave={mockOnSave} />);

      await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');
      await user.click(screen.getByTestId('priority-dropdown'));

      // when
      await user.click(screen.getByTestId(`priority-dropdown-option-${TaskPriority.HIGH}`));
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(createTaskSpy).toHaveBeenCalledWith(
          expect.objectContaining({ priority: TaskPriority.HIGH })
        );
      });
    });
  });
});

