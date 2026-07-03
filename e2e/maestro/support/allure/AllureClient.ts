import { AllureTestReporter } from '@/support/allure/AllureTestReporter';
import { buildMaestroEnvironmentInfo } from '@/support/allure/environmentInfo';
import type { TestSuite } from '@/runner/types';

type TestMetadata = {
  name: string;
  epic: string;
  feature: string;
  tms: string;
};

type TestRunHelpers = {
  step: (name: string, action: () => Promise<void>) => Promise<void>;
  addMaestroSteps: (outputDir: string, runnerEnv: Record<string, string>) => void;
};

export class AllureClient {
  private readonly reporter: AllureTestReporter;
  private runtimeDeviceInfo: Record<string, string> = {};

  constructor(resultsDir: string, suite: TestSuite) {
    this.reporter = new AllureTestReporter(resultsDir, {
      environmentInfo: buildMaestroEnvironmentInfo(suite),
    });
  }

  setRuntimeDeviceInfo(info: Record<string, string>): void {
    this.runtimeDeviceInfo = info;
  }

  runTest(
    metadata: TestMetadata,
    fn: (helpers: TestRunHelpers) => Promise<void>,
  ): Promise<void> {
    return this.reporter.runTest(metadata, fn);
  }

  writeEnvironmentInfo(): void {
    if (Object.keys(this.runtimeDeviceInfo).length > 0) {
      this.reporter.mergeEnvironmentInfo(this.runtimeDeviceInfo);
    }
    this.reporter.writeEnvironmentInfo();
  }
}
