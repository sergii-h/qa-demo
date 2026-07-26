import { createBdd } from 'playwright-bdd';
import { test } from './scenario';

export { test };
export { expect } from '@playwright/test';
export const { Given, When, Then, Before, BeforeAll } = createBdd(test);
