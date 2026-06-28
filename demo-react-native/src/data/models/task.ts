export enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE',
}

export enum TaskPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

export interface Task {
  id: string;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  createdDate: string | null;
  updatedDate: string | null;
}

export interface TaskRequest {
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
}

export interface ErrorResponse {
  message?: string;
}
