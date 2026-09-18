export enum TaskStatus {
    TODO = 'TODO',
    IN_PROGRESS = 'IN_PROGRESS',
    DONE = 'DONE'
}

export enum TaskPriority {
    LOW = 'LOW',
    MEDIUM = 'MEDIUM',
    HIGH = 'HIGH'
}

export default interface ITask {
    id?: string;
    title: string;
    description?: string;
    status: TaskStatus;
    priority: TaskPriority;
    createdDate?: string;
    updatedDate?: string;
}

// Built-in Omit's second parameter is `keyof any`, not `keyof T` — a typo'd or renamed
// key here would silently stop being omitted instead of failing to compile.
type StrictOmit<T, K extends keyof T> = Omit<T, K>;

export type TaskWrite = StrictOmit<ITask, "id" | "createdDate" | "updatedDate">;

