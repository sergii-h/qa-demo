import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TasksTable from './tasksTable';
import * as services from '../../services';
import { TaskStatus, TaskPriority } from '../../interfaces/ITask';

vi.mock('../infoTaskModal', () => ({
    default: ({ taskId, onClose }: any) => (
        <div data-testid="info-modal" data-task-id={taskId}>
            <button data-testid="close-info-modal" onClick={onClose}>Close</button>
        </div>
    ),
}));

vi.mock('../editTaskModal', () => ({
    default: ({ taskId, onClose, onSave }: any) => (
        <div data-testid="edit-modal" data-task-id={taskId}>
            <button data-testid="save-edit-modal" onClick={onSave}>Save</button>
            <button data-testid="close-edit-modal" onClick={onClose}>Close</button>
        </div>
    ),
}));

vi.mock('../createTaskModal', () => ({
    default: ({ onClose, onSave }: any) => (
        <div data-testid="create-modal">
            <button data-testid="save-create-modal" onClick={onSave}>Save</button>
            <button data-testid="close-create-modal" onClick={onClose}>Close</button>
        </div>
    ),
}));

describe('TasksTable', () => {
    const mockTasks = [
        { id: '1', title: 'Task One', status: TaskStatus.TODO, priority: TaskPriority.LOW },
        { id: '2', title: 'Task Two', status: TaskStatus.IN_PROGRESS, priority: TaskPriority.MEDIUM },
        { id: '3', title: 'Task Three', status: TaskStatus.DONE, priority: TaskPriority.HIGH },
    ];

    beforeEach(() => {
        vi.spyOn(services, 'getTasks').mockResolvedValue(mockTasks);
        vi.spyOn(services, 'deleteTask').mockResolvedValue(undefined);
    });

    describe('Rendering', () => {
        it('should fetch and display tasks on mount', async () => {
            render(<TasksTable />);

            await waitFor(() => {
                expect(screen.getByTestId('task-title-1')).toHaveTextContent('Task One');
                expect(screen.getByTestId('task-title-2')).toHaveTextContent('Task Two');
                expect(screen.getByTestId('task-title-3')).toHaveTextContent('Task Three');
            });
        });

        it('should render Create task button', async () => {
            render(<TasksTable />);

            expect(screen.getByTestId('add-task-button')).toBeInTheDocument();
        });

        it('should render all status tags', async () => {
            render(<TasksTable />);

            await waitFor(() => {
                expect(screen.getByTestId(`status-tag-${TaskStatus.TODO}`)).toBeInTheDocument();
                expect(screen.getByTestId(`status-tag-${TaskStatus.IN_PROGRESS}`)).toBeInTheDocument();
                expect(screen.getByTestId(`status-tag-${TaskStatus.DONE}`)).toBeInTheDocument();
            });
        });

        it('should render all priority tags', async () => {
            render(<TasksTable />);

            await waitFor(() => {
                expect(screen.getByTestId(`priority-tag-${TaskPriority.LOW}`)).toBeInTheDocument();
                expect(screen.getByTestId(`priority-tag-${TaskPriority.MEDIUM}`)).toBeInTheDocument();
                expect(screen.getByTestId(`priority-tag-${TaskPriority.HIGH}`)).toBeInTheDocument();
            });
        });
    });

    describe('Info Modal', () => {
        it('should open info modal with correct taskId when Info button clicked', async () => {
            const user = userEvent.setup();
            render(<TasksTable />);

            await waitFor(() => expect(screen.getByTestId('info-button-1')).toBeInTheDocument());

            await user.click(screen.getByTestId('info-button-1'));

            expect(screen.getByTestId('info-modal')).toBeInTheDocument();
            expect(screen.getByTestId('info-modal')).toHaveAttribute('data-task-id', '1');
        });

        it('should close info modal when onClose is called', async () => {
            const user = userEvent.setup();
            render(<TasksTable />);

            await waitFor(() => expect(screen.getByTestId('info-button-1')).toBeInTheDocument());

            await user.click(screen.getByTestId('info-button-1'));
            expect(screen.getByTestId('info-modal')).toBeInTheDocument();

            await user.click(screen.getByTestId('close-info-modal'));
            expect(screen.queryByTestId('info-modal')).not.toBeInTheDocument();
        });
    });

    describe('Edit Modal', () => {
        it('should open edit modal with correct taskId when Edit button clicked', async () => {
            const user = userEvent.setup();
            render(<TasksTable />);

            await waitFor(() => expect(screen.getByTestId('edit-button-2')).toBeInTheDocument());

            await user.click(screen.getByTestId('edit-button-2'));

            expect(screen.getByTestId('edit-modal')).toBeInTheDocument();
            expect(screen.getByTestId('edit-modal')).toHaveAttribute('data-task-id', '2');
        });

        it('should close edit modal when onClose is called', async () => {
            const user = userEvent.setup();
            render(<TasksTable />);

            await waitFor(() => expect(screen.getByTestId('edit-button-1')).toBeInTheDocument());

            await user.click(screen.getByTestId('edit-button-1'));
            await user.click(screen.getByTestId('close-edit-modal'));

            expect(screen.queryByTestId('edit-modal')).not.toBeInTheDocument();
        });

        it('should refresh tasks when edit modal onSave is called', async () => {
            const user = userEvent.setup();
            const getTasksSpy = vi.spyOn(services, 'getTasks').mockResolvedValue(mockTasks);
            render(<TasksTable />);

            await waitFor(() => expect(screen.getByTestId('edit-button-1')).toBeInTheDocument());

            await user.click(screen.getByTestId('edit-button-1'));
            await user.click(screen.getByTestId('save-edit-modal'));

            await waitFor(() => {
                expect(getTasksSpy).toHaveBeenCalledTimes(2);
            });
        });
    });

    describe('Create Modal', () => {
        it('should open create modal when Create task button is clicked', async () => {
            const user = userEvent.setup();
            render(<TasksTable />);

            await user.click(screen.getByTestId('add-task-button'));

            expect(screen.getByTestId('create-modal')).toBeInTheDocument();
        });

        it('should close create modal when onClose is called', async () => {
            const user = userEvent.setup();
            render(<TasksTable />);

            await user.click(screen.getByTestId('add-task-button'));
            await user.click(screen.getByTestId('close-create-modal'));

            expect(screen.queryByTestId('create-modal')).not.toBeInTheDocument();
        });

        it('should refresh tasks when create modal onSave is called', async () => {
            const user = userEvent.setup();
            const getTasksSpy = vi.spyOn(services, 'getTasks').mockResolvedValue(mockTasks);
            render(<TasksTable />);

            await user.click(screen.getByTestId('add-task-button'));
            await user.click(screen.getByTestId('save-create-modal'));

            await waitFor(() => {
                expect(getTasksSpy).toHaveBeenCalledTimes(2);
            });
        });
    });

    describe('Delete', () => {
        it('should call deleteTask and remove task from table when Delete button clicked', async () => {
            const user = userEvent.setup();
            render(<TasksTable />);

            await waitFor(() => expect(screen.getByTestId('delete-button-1')).toBeInTheDocument());

            await user.click(screen.getByTestId('delete-button-1'));

            await waitFor(() => {
                expect(services.deleteTask).toHaveBeenCalledWith('1');
            });
        });
    });
});
