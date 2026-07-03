import { TaskPriority } from './TaskPriority';
import { TaskStatus } from './TaskStatus';

export type WireMockTask = {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  createdDate: string;
  updatedDate: string;
};

export type TaskResponse = {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
};

const MOCK_CREATED_DATE = '2024-01-01T10:00:00Z';
const MOCK_UPDATED_DATE = '2024-01-02T10:00:00Z';

export function toWireMockTask(response: TaskResponse): WireMockTask {
  return {
    id: response.id,
    title: response.title,
    description: response.description,
    status: response.status,
    priority: response.priority,
    createdDate: MOCK_CREATED_DATE,
    updatedDate: MOCK_UPDATED_DATE,
  };
}
