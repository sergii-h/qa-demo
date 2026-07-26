import { test as base } from 'playwright-bdd';

type DeviceFixtures = {
  isMobile: boolean;
};

export const test = base.extend<DeviceFixtures>({
  isMobile: async ({}, use, testInfo) => {
    await use(testInfo.project.use.isMobile ?? false);
  },
});
