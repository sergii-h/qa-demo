import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

import { TaskContext } from '@/context/TaskContext';
import { SupportProvider } from '@/providers/SupportProvider';
import { testsForSuite } from '@/runner/testRegistry';
import type { TestSuite } from '@/runner/types';
import { resolveMaestroCli } from '@/support/maestroCli';
import { parseMaestroDeviceOutput } from '@/support/deviceInfo';
import { testConfig } from '@/test.config';

export class MaestroTestRunner {
  private readonly maestroCli = resolveMaestroCli();
  private runtimeDeviceInfo?: Record<string, string>;

  constructor(
    private readonly maestroRoot: string,
    private readonly resultsDir: string,
    private readonly maestroOutputRoot: string,
  ) {}

  async run(suite: TestSuite): Promise<void> {
    fs.mkdirSync(this.maestroOutputRoot, { recursive: true });

    const support = new SupportProvider(this.resultsDir, suite);
    const selectedTests = await testsForSuite(this.maestroRoot, suite);
    const requiresWireMock = suite === 'mocked' || suite === 'accessibility';
    let failed = false;

    for (const testCase of selectedTests) {
      try {
        if (requiresWireMock && testCase.setupMocks) {
          await support.wiremock.ensureReady();
        }

        await support.allure.runTest(
          {
            name: testCase.name,
            epic: testCase.epic,
            feature: testCase.feature,
            tms: testCase.tms,
          },
          async ({ step, addMaestroSteps }) => {
            const context = new TaskContext();
            let env: Record<string, string> = context.toMaestroEnv();

            if (testCase.setupMocks) {
              await step('Setup WireMock stubs', async () => {
                env = await testCase.setupMocks!(support, context);
              });
            }

            const outputDir = this.createMaestroOutputDir(testCase.flow);
            try {
              this.runMaestroFlow(testCase.flow, env, outputDir);
            } finally {
              addMaestroSteps(outputDir, env);
            }
          },
        );
      } catch {
        failed = true;
      }
    }

    support.allure.setRuntimeDeviceInfo(this.runtimeDeviceInfo ?? {});
    support.allure.writeEnvironmentInfo();

    if (failed) {
      process.exit(1);
    }
  }

  private createMaestroOutputDir(flowPath: string): string {
    const flowBaseName = path.basename(flowPath, path.extname(flowPath));
    const outputDir = path.join(
      this.maestroOutputRoot,
      `${flowBaseName}-${Date.now()}-${process.pid}`,
    );
    fs.mkdirSync(outputDir, { recursive: true });
    return outputDir;
  }

  private runMaestroFlow(
    flowPath: string,
    env: Record<string, string>,
    outputDir: string,
  ): void {
    const absoluteFlow = path.resolve(this.maestroRoot, flowPath);
    const maestroEnv = {
      ...env,
      MAESTRO_APP_ID: testConfig.maestro.appId,
    };
    const args = [
      ...(process.env.MAESTRO_DEVICE
        ? ['--device', process.env.MAESTRO_DEVICE]
        : []),
      'test',
      ...Object.entries(maestroEnv).flatMap(([key, value]) => ['-e', `${key}=${value}`]),
      '--test-output-dir',
      outputDir,
      absoluteFlow,
    ];

    const maxAttempts = process.env.MAESTRO_DEVICE ? 2 : 1;

    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      const result = spawnSync(this.maestroCli, args, {
        encoding: 'utf8',
        stdio: ['ignore', 'pipe', 'pipe'],
        env: {
          ...process.env,
          MAESTRO_CLI_NO_ANALYTICS: 'true',
          MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED: 'true',
        },
      });

      if (result.stdout) {
        process.stdout.write(result.stdout);
      }
      if (result.stderr) {
        process.stderr.write(result.stderr);
      }

      const combinedOutput = `${result.stdout ?? ''}\n${result.stderr ?? ''}`;
      const deviceInfo = parseMaestroDeviceOutput(combinedOutput);
      if (deviceInfo) {
        this.runtimeDeviceInfo = deviceInfo;
      }

      if (result.status === 0) {
        return;
      }

      const isDriverStartupTimeout = combinedOutput.includes(
        'IOSDriverTimeoutException',
      );
      if (isDriverStartupTimeout && attempt < maxAttempts) {
        process.stderr.write(
          `\nMaestro iOS driver startup timed out (attempt ${attempt}/${maxAttempts}), retrying...\n`,
        );
        continue;
      }

      throw new Error(result.stderr || result.stdout || 'Maestro test failed');
    }
  }
}
