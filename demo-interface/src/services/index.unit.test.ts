import * as services from './index';
import { TaskStatus, TaskPriority } from '../interfaces/ITask';

const mockTask = {
    id: '1',
    title: 'Test Task',
    description: 'Test Description',
    status: TaskStatus.TODO,
    priority: TaskPriority.MEDIUM,
};

describe('services', () => {
    let mockFetch: ReturnType<typeof vi.fn>;

    beforeEach(() => {
        mockFetch = vi.fn();
        vi.stubGlobal('fetch', mockFetch);
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    describe('getTasks', () => {
        it('should fetch all tasks', async () => {
            mockFetch.mockResolvedValue({ json: () => Promise.resolve([mockTask]) });

            const result = await services.getTasks();

            expect(mockFetch).toHaveBeenCalledWith(
                `${services.BE_API}/tasks`,
                { method: 'GET', headers: { 'Content-Type': 'application/json' } }
            );
            expect(result).toEqual([mockTask]);
        });
    });

    describe('getTask', () => {
        it('should fetch a single task by id', async () => {
            mockFetch.mockResolvedValue({ ok: true, json: () => Promise.resolve(mockTask) });

            const result = await services.getTask('1');

            expect(mockFetch).toHaveBeenCalledWith(
                `${services.BE_API}/tasks/1`,
                { method: 'GET', headers: { 'Content-Type': 'application/json' } }
            );
            expect(result).toEqual(mockTask);
        });

        it('should throw error with message from response when not ok', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                status: 500,
                json: () => Promise.resolve({ message: 'Task not found' }),
            });

            await expect(services.getTask('1')).rejects.toThrow('Task not found');
        });

        it('should throw default error message when response has no message', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                status: 500,
                json: () => Promise.resolve({}),
            });

            await expect(services.getTask('1')).rejects.toThrow('Request failed (500)');
        });
    });

    describe('getIsValid', () => {
        it('should return true when task is valid', async () => {
            mockFetch.mockResolvedValue({ ok: true, json: () => Promise.resolve(true) });

            const result = await services.getIsValid('1');

            expect(mockFetch).toHaveBeenCalledWith(
                `${services.BE_API}/tasks/isValid/1`,
                { method: 'GET', headers: { 'Content-Type': 'application/json' } }
            );
            expect(result).toBe(true);
        });

        it('should return false when task is not valid', async () => {
            mockFetch.mockResolvedValue({ ok: true, json: () => Promise.resolve(false) });

            const result = await services.getIsValid('1');

            expect(result).toBe(false);
        });

        it('should throw error with message from response when not ok', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                status: 500,
                json: () => Promise.resolve({ message: 'Validation failed' }),
            });

            await expect(services.getIsValid('1')).rejects.toThrow('Validation failed');
        });

        it('should throw default error message when response has no message', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                status: 500,
                json: () => Promise.resolve({}),
            });

            await expect(services.getIsValid('1')).rejects.toThrow('Request failed (500)');
        });
    });

    describe('createTask', () => {
        it('should create a task and return it', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(mockTask),
            });

            const result = await services.createTask(mockTask);

            expect(mockFetch).toHaveBeenCalledWith(
                `${services.BE_API}/tasks`,
                {
                    method: 'POST',
                    body: JSON.stringify(mockTask),
                    headers: { 'Content-Type': 'application/json' },
                }
            );
            expect(result).toEqual(mockTask);
        });

        it('should throw error with message from response when not ok', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                json: () => Promise.resolve({ message: 'Task already exists' }),
            });

            await expect(services.createTask(mockTask)).rejects.toThrow('Task already exists');
        });

        it('should throw default error message when response has no message', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                json: () => Promise.resolve({}),
            });

            await expect(services.createTask(mockTask)).rejects.toThrow('Failed to create task');
        });
    });

    describe('updateTask', () => {
        it('should update a task and return it', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(mockTask),
            });

            const result = await services.updateTask(mockTask);

            expect(mockFetch).toHaveBeenCalledWith(
                `${services.BE_API}/tasks/${mockTask.id}`,
                {
                    method: 'PUT',
                    body: JSON.stringify({
                        title: mockTask.title,
                        description: mockTask.description,
                        status: mockTask.status,
                        priority: mockTask.priority,
                    }),
                    headers: { 'Content-Type': 'application/json' },
                }
            );
            expect(result).toEqual(mockTask);
        });

        it('should throw error with message from response when not ok', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                json: () => Promise.resolve({ message: 'Task not found' }),
            });

            await expect(services.updateTask(mockTask)).rejects.toThrow('Task not found');
        });

        it('should throw default error message when response has no message', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                json: () => Promise.resolve({}),
            });

            await expect(services.updateTask(mockTask)).rejects.toThrow('Failed to update task');
        });
    });

    describe('deleteTask', () => {
        it('should delete a task by id', async () => {
            const mockResponse = { status: 204 };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await services.deleteTask('1');

            expect(mockFetch).toHaveBeenCalledWith(
                `${services.BE_API}/tasks/1`,
                { method: 'DELETE', headers: { 'Content-Type': 'application/json' } }
            );
            expect(result).toEqual(mockResponse);
        });
    });
});
