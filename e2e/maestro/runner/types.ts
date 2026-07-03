import { TaskContext } from '@/context/TaskContext';
import { AllureEpic } from '@/data/AllureEpic';
import { SupportProvider } from '@/providers/SupportProvider';

export type TestSuite = 'mocked' | 'uat' | 'accessibility';

export type MaestroTestCase = {
  name: string;
  flow: string;
  epic: AllureEpic;
  feature: string;
  tms: string;
  suite: TestSuite;
  setupMocks?: (
    support: SupportProvider,
    context: TaskContext,
  ) => Promise<Record<string, string>>;
};
