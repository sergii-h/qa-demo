import { SupportProvider } from '@/providers/SupportProvider';
import { StepProvider } from '@/providers/StepProvider';
import { ValidationProvider } from '@/providers/ValidationProvider';
import { test as base } from './device';

type Fixtures = {
  step: StepProvider;
  validate: ValidationProvider;
  support: SupportProvider;
};

export const test = base.extend<Fixtures>({
  step: async ({ page, isMobile, request }, use) => {
    await use(new StepProvider(page, isMobile, request));
  },

  validate: async ({ page, isMobile }, use) => {
    await use(new ValidationProvider(page, isMobile));
  },

  support: async ({ page, request }, use) => {
    await use(new SupportProvider(page, request));
  },
});
