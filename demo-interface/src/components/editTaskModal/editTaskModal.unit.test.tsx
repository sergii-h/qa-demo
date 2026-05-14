import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import EditTaskModal from './editTaskModal';
import * as services from '../../services';
import { TaskStatus, TaskPriority } from '../../interfaces/ITask';

describe('EditTaskModal', () => {
    const mockOnClose = vi.fn();
    const mockOnSave = vi.fn();
    const taskId = 'task-123';

    const mockTask = {
        id: taskId,
        title: 'Existing Task',
        description: 'Existing Description',
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.HIGH,
    };

    beforeEach(() => {
        vi.spyOn(services, 'getTask').mockResolvedValue(mockTask);
    });

    describe('Rendering', () => {
        it('should show loading spinner while fetching task', () => {
            vi.spyOn(services, 'getTask').mockReturnValue(new Promise(() => {}));

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
        });

        it('should render form with task data after loading', async () => {
            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => {
                expect(screen.getByTestId('edit-task-title-input')).toHaveValue('Existing Task');
            });
            expect(screen.getByLabelText(/description/i)).toHaveValue('Existing Description');
        });

        it('should use empty string for description when task has none', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, description: undefined });

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => {
                expect(screen.getByLabelText(/description/i)).toHaveValue('');
            });
        });

        it('should show "Edit" header before task loads', () => {
            vi.spyOn(services, 'getTask').mockReturnValue(new Promise(() => {}));

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            expect(screen.getByTestId('modal-title')).toHaveTextContent('Edit');
        });

        it('should show task title in header after task loads', async () => {
            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => {
                expect(screen.getByTestId('modal-title')).toHaveTextContent(`Edit ${mockTask.title}`);
            });
        });
    });

    describe('Title Validation', () => {
        it('should disable Save button when title is empty', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, title: '' });

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => {
                expect(screen.getByTestId('save-button')).toBeDisabled();
            });
        });

        it('should show error when title exceeds 100 characters', async () => {
            const user = userEvent.setup();
            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('edit-task-title-input')).toBeInTheDocument());

            await user.clear(screen.getByTestId('edit-task-title-input'));
            await user.type(screen.getByTestId('edit-task-title-input'), 'a'.repeat(101));
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => {
                expect(screen.getByTestId('title-error')).toHaveTextContent('Title must not exceed 100 characters');
            });
        });

        it('should clear title error when user types after error', async () => {
            const user = userEvent.setup();
            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('edit-task-title-input')).toBeInTheDocument());

            await user.clear(screen.getByTestId('edit-task-title-input'));
            await user.type(screen.getByTestId('edit-task-title-input'), 'a'.repeat(101));
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => expect(screen.getByTestId('title-error')).toBeInTheDocument());

            await user.clear(screen.getByTestId('edit-task-title-input'));
            await user.type(screen.getByTestId('edit-task-title-input'), 'Valid title');

            expect(screen.queryByTestId('title-error')).not.toBeInTheDocument();
        });
    });

    describe('Task Update', () => {
        it('should call updateTask with correct data', async () => {
            const user = userEvent.setup();
            const updateTaskSpy = vi.spyOn(services, 'updateTask').mockResolvedValue(mockTask);

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('edit-task-title-input')).toBeInTheDocument());

            await user.clear(screen.getByTestId('edit-task-title-input'));
            await user.type(screen.getByTestId('edit-task-title-input'), 'Updated Title');
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => {
                expect(updateTaskSpy).toHaveBeenCalledWith({
                    id: taskId,
                    title: 'Updated Title',
                    description: mockTask.description,
                    status: mockTask.status,
                    priority: mockTask.priority,
                });
            });
        });

        it('should call onSave and onClose after successful update', async () => {
            const user = userEvent.setup();
            vi.spyOn(services, 'updateTask').mockResolvedValue(mockTask);

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('save-button')).toBeInTheDocument());
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => {
                expect(mockOnSave).toHaveBeenCalledTimes(1);
                expect(mockOnClose).toHaveBeenCalledTimes(1);
            });
        });

        it('should update description when user types', async () => {
            const user = userEvent.setup();
            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByLabelText(/description/i)).toBeInTheDocument());

            await user.clear(screen.getByLabelText(/description/i));
            await user.type(screen.getByLabelText(/description/i), 'New description');

            expect(screen.getByLabelText(/description/i)).toHaveValue('New description');
        });

        it('should update status when dropdown changes', async () => {
            const user = userEvent.setup();
            const updateTaskSpy = vi.spyOn(services, 'updateTask').mockResolvedValue(mockTask);

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('status-dropdown')).toBeInTheDocument());
            await user.click(screen.getByTestId('status-dropdown'));
            await user.click(await screen.findByRole('option', { name: 'Done', hidden: true }));
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => {
                expect(updateTaskSpy).toHaveBeenCalledWith(expect.objectContaining({ status: TaskStatus.DONE }));
            });
        });

        it('should update priority when dropdown changes', async () => {
            const user = userEvent.setup();
            const updateTaskSpy = vi.spyOn(services, 'updateTask').mockResolvedValue(mockTask);

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('priority-dropdown')).toBeInTheDocument());
            await user.click(screen.getByTestId('priority-dropdown'));
            await user.click(await screen.findByRole('option', { name: 'Low', hidden: true }));
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => {
                expect(updateTaskSpy).toHaveBeenCalledWith(expect.objectContaining({ priority: TaskPriority.LOW }));
            });
        });

        it('should call updateTask only once on rapid double submit', async () => {
            const user = userEvent.setup();
            let resolveUpdateTask: ((value: typeof mockTask) => void) | undefined;

            const updateTaskSpy = vi.spyOn(services, 'updateTask').mockImplementation(() =>
                new Promise((resolve) => {
                    resolveUpdateTask = resolve;
                })
            );

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('save-button')).toBeInTheDocument());

            // when
            await user.dblClick(screen.getByTestId('save-button'));

            // then
            expect(updateTaskSpy).toHaveBeenCalledTimes(1);

            resolveUpdateTask?.(mockTask);

            await waitFor(() => {
                expect(mockOnSave).toHaveBeenCalledTimes(1);
                expect(mockOnClose).toHaveBeenCalledTimes(1);
            });
        });
    });

    describe('Error Handling', () => {
        it('should show error when task title already exists', async () => {
            const user = userEvent.setup();
            vi.spyOn(services, 'updateTask').mockRejectedValue(
                new Error("Task with title 'Updated Title' already exists")
            );

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('save-button')).toBeInTheDocument());
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => {
                expect(screen.getByTestId('title-error')).toHaveTextContent('Task with this title already exists');
            });
            expect(mockOnSave).not.toHaveBeenCalled();
            expect(mockOnClose).not.toHaveBeenCalled();
        });

        it('should show generic error when update fails for unknown reason', async () => {
            const user = userEvent.setup();
            vi.spyOn(services, 'updateTask').mockRejectedValue(new Error('Network error'));

            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('save-button')).toBeInTheDocument());
            await user.click(screen.getByTestId('save-button'));

            await waitFor(() => {
                expect(screen.getByTestId('title-error')).toHaveTextContent('Failed to update task. Please try again.');
            });
        });
    });

    describe('User Interactions', () => {
        it('should call onClose when Close button is clicked', async () => {
            const user = userEvent.setup();
            render(<EditTaskModal taskId={taskId} onClose={mockOnClose} onSave={mockOnSave} />);

            await waitFor(() => expect(screen.getByTestId('close-button')).toBeInTheDocument());
            await user.click(screen.getByTestId('close-button'));

            expect(mockOnClose).toHaveBeenCalledTimes(1);
        });
    });
});
