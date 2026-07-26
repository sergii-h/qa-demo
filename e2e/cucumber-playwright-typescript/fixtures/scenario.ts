import type { AxeResults } from 'axe-core';
import { TaskContext } from '@/context/TaskContext';
import { test as base } from './actions';

export type ScenarioState = {
  taskContext: TaskContext;
  updatedTaskContext?: TaskContext;
  axeResults?: AxeResults;
};

export const test = base.extend<{ scenario: ScenarioState }>({
  scenario: async ({}, use) => {
    await use({ taskContext: new TaskContext() });
  },
});
