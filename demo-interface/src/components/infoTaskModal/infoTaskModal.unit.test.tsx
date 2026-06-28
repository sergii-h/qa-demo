import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InfoTaskModal from './infoTaskModal';
import * as services from '../../services';
import { TaskStatus, TaskPriority } from '../../interfaces/ITask';

describe('InfoTaskModal', () => {
    const mockOnClose = vi.fn();
    const taskId = 'task-123';

    const mockTask = {
        id: taskId,
        title: 'My Task',
        description: 'My Description',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
        createdDate: '2024-01-15T10:00:00.000Z',
        updatedDate: '2024-01-16T12:00:00.000Z',
    };

    beforeEach(() => {
        vi.spyOn(services, 'getTask').mockResolvedValue(mockTask);
        vi.spyOn(services, 'getIsValid').mockResolvedValue(false);
    });

    describe('Rendering', () => {
        it('should show loading spinner while fetching', () => {
            vi.spyOn(services, 'getTask').mockReturnValue(new Promise(() => {}));

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
        });

        it('should render task details after loading', async () => {
            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('description')).toHaveTextContent('My Description');
            });
            expect(screen.getByTestId('modal-title')).toHaveTextContent('My Task');
        });

        it('should show "No description" when task has no description', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, description: undefined });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('description')).toHaveTextContent('No description');
            });
        });

        it('should show "Info" as header before task loads', () => {
            vi.spyOn(services, 'getTask').mockReturnValue(new Promise(() => {}));

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            expect(screen.getByTestId('modal-title')).toHaveTextContent('Info');
        });

        it('should show formatted createdDate', async () => {
            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('created-date')).toHaveTextContent(
                    new Date(mockTask.createdDate!).toLocaleString()
                );
            });
        });

        it('should show formatted updatedDate', async () => {
            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('updated-date')).toHaveTextContent(
                    new Date(mockTask.updatedDate!).toLocaleString()
                );
            });
        });

        it('should show N/A when createdDate is missing', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, createdDate: undefined });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('created-date')).toHaveTextContent('N/A');
            });
        });

        it('should show N/A when updatedDate is missing', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, updatedDate: undefined });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('updated-date')).toHaveTextContent('N/A');
            });
        });
    });

    describe('Status severity', () => {
        it('should render TODO status tag', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, status: TaskStatus.TODO });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId(`status-tag-${TaskStatus.TODO}`)).toBeInTheDocument();
            });
        });

        it('should render IN_PROGRESS status tag', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, status: TaskStatus.IN_PROGRESS });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId(`status-tag-${TaskStatus.IN_PROGRESS}`)).toBeInTheDocument();
            });
        });

        it('should render DONE status tag', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, status: TaskStatus.DONE });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId(`status-tag-${TaskStatus.DONE}`)).toBeInTheDocument();
            });
        });
    });

    describe('Priority severity', () => {
        it('should render LOW priority tag', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, priority: TaskPriority.LOW });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId(`priority-tag-${TaskPriority.LOW}`)).toBeInTheDocument();
            });
        });

        it('should render MEDIUM priority tag', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, priority: TaskPriority.MEDIUM });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId(`priority-tag-${TaskPriority.MEDIUM}`)).toBeInTheDocument();
            });
        });

        it('should render HIGH priority tag', async () => {
            vi.spyOn(services, 'getTask').mockResolvedValue({ ...mockTask, priority: TaskPriority.HIGH });

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId(`priority-tag-${TaskPriority.HIGH}`)).toBeInTheDocument();
            });
        });
    });

    describe('Error handling', () => {
        it('should show load error when task fetch fails', async () => {
            vi.spyOn(services, 'getTask').mockRejectedValue(new Error('Request failed (500)'));

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('load-error')).toHaveTextContent('Failed to load task info. Please try again.');
            });
            expect(screen.queryByTestId('description')).not.toBeInTheDocument();
        });

        it('should show Info as title when task fetch fails', async () => {
            vi.spyOn(services, 'getTask').mockRejectedValue(new Error('Request failed (500)'));

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('load-error')).toBeInTheDocument();
            });
            expect(screen.getByTestId('modal-title')).toHaveTextContent('Info');
        });

        it('should show task details with not-valid indicator when validation fetch fails', async () => {
            vi.spyOn(services, 'getIsValid').mockRejectedValue(new Error('Request failed (500)'));

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('description')).toHaveTextContent(mockTask.description);
            });
            expect(screen.getByTestId('notValid')).toBeInTheDocument();
            expect(screen.queryByTestId('valid')).not.toBeInTheDocument();
            expect(screen.queryByTestId('load-error')).not.toBeInTheDocument();
        });

        it('should call onClose when close button is clicked after task fetch fails', async () => {
            const user = userEvent.setup();
            vi.spyOn(services, 'getTask').mockRejectedValue(new Error('Request failed (500)'));

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('load-error')).toBeInTheDocument();
            });

            await user.click(screen.getByTestId('close-button'));

            expect(mockOnClose).toHaveBeenCalledTimes(1);
        });
    });

    describe('Validation indicator', () => {
        it('should show valid icon when task is valid', async () => {
            vi.spyOn(services, 'getIsValid').mockResolvedValue(true);

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('valid')).toBeInTheDocument();
            });
        });

        it('should show not-valid icon when task is not valid', async () => {
            vi.spyOn(services, 'getIsValid').mockResolvedValue(false);

            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => {
                expect(screen.getByTestId('notValid')).toBeInTheDocument();
            });
        });
    });

    describe('User Interactions', () => {
        it('should call onClose when dialog is hidden', async () => {
            const user = userEvent.setup();
            render(<InfoTaskModal taskId={taskId} onClose={mockOnClose} />);

            await waitFor(() => expect(screen.getByTestId('modal-title')).toHaveTextContent('My Task'));

            await user.click(screen.getByTestId('close-button'));

            expect(mockOnClose).toHaveBeenCalledTimes(1);
        });
    });
});
