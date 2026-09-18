import path from 'node:path';
import {MatchersV3, PactV4, SpecificationVersion} from '@pact-foundation/pact';

import {Task} from '@/data/models/task';

const { like, regex } = MatchersV3;

export const createPact = (provider: string) =>
  new PactV4({
    consumer: 'demo-react-native',
    provider,
    dir: path.resolve(process.cwd(), 'pacts'),
    spec: SpecificationVersion.SPECIFICATION_VERSION_V3,
  });

export const TASK_ID = '507f1f77bcf86cd799439011';
export const timestampPattern = '^\\d{4}-\\d{2}-\\d{2}T.*$';

export function likeTask(task: Task): { [K in keyof Task]: unknown } {
  return {
    id: regex('^[a-f0-9]{24}$', task.id),
    title: like(task.title),
    description: like(task.description),
    status: like(task.status),
    priority: like(task.priority),
    createdDate: regex(timestampPattern, task.createdDate ?? ''),
    updatedDate: regex(timestampPattern, task.updatedDate ?? ''),
  };
}
