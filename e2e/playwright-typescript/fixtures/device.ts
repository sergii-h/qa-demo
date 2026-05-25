import { test as base } from '@playwright/test';

type DeviceFixtures = {
  isMobile: boolean;
};

export const test = base.extend<DeviceFixtures>({
  isMobile: async ({}, use, testInfo) => {
    await use(testInfo.project.use.isMobile ?? false);
  },
});
