import { mergeTests } from '@playwright/test';
import { test as actionsTest } from './actions';
import { test as deviceTest } from './device';

export const test = mergeTests(deviceTest, actionsTest);
export { expect } from '@playwright/test';
