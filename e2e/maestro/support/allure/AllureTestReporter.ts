import path from 'node:path';

import {
  FileSystemWriter,
  ReporterRuntime,
} from 'allure-js-commons/sdk/reporter';
import {
  ContentType,
  LabelName,
  LinkType,
  Stage,
  Status,
  type StepResult,
  type TestResult,
} from 'allure-js-commons';
import type { EnvironmentInfo } from 'allure-js-commons/sdk';

import {
  buildMaestroAllureSteps,
  findLatestMaestroFailureScreenshot,
  readMaestroCommands,
  resolveMaestroCommandsPath,
  type MaestroAllureStep,
} from '@/support/allure/maestroSteps';

type AllureTestReporterOptions = {
  environmentInfo?: EnvironmentInfo;
};

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

export class AllureTestReporter {
  private readonly runtime: ReporterRuntime;
  private lastMaestroSearchRoot?: string;
  private testFailureScreenshotAttached = false;

  constructor(resultsDir: string, options: AllureTestReporterOptions = {}) {
    this.runtime = new ReporterRuntime({
      writer: new FileSystemWriter({ resultsDir }),
      environmentInfo: options.environmentInfo,
    });
  }

  mergeEnvironmentInfo(info: EnvironmentInfo): void {
    this.runtime.environmentInfo = {
      ...this.runtime.environmentInfo,
      ...info,
    };
  }

  writeEnvironmentInfo(): void {
    this.runtime.writeEnvironmentInfo();
  }

  async runTest(
    metadata: TestMetadata,
    fn: (helpers: TestRunHelpers) => Promise<void>,
  ): Promise<void> {
    this.lastMaestroSearchRoot = undefined;
    this.testFailureScreenshotAttached = false;

    const testUuid = this.runtime.startTest({
      name: metadata.name,
      historyId: `${metadata.epic}/${metadata.feature}/${metadata.name}`,
      fullName: `${metadata.epic}.${metadata.feature}.${metadata.name}`,
      testCaseId: metadata.tms,
      labels: [
        { name: LabelName.EPIC, value: metadata.epic },
        { name: LabelName.FEATURE, value: metadata.feature },
      ],
      links: [{ type: LinkType.TMS, url: metadata.tms, name: metadata.tms }],
    });

    const runStep = async (
      name: string,
      action: () => Promise<void>,
    ): Promise<void> => {
      const stepUuid = this.runtime.startStep(testUuid, null, { name });
      if (!stepUuid) {
        await action();
        return;
      }

      try {
        await action();
        this.runtime.updateStep(stepUuid, (step: StepResult) => {
          step.status = Status.PASSED;
          step.stage = Stage.FINISHED;
        });
      } catch (error) {
        this.runtime.updateStep(stepUuid, (step: StepResult) => {
          step.status = Status.FAILED;
          step.stage = Stage.FINISHED;
          step.statusDetails = { message: String(error) };
        });
        throw error;
      } finally {
        this.runtime.stopStep(stepUuid);
      }
    };

    const addMaestroSteps = (outputDir: string, runnerEnv: Record<string, string>): void => {
      this.importMaestroSteps(testUuid, outputDir, runnerEnv);
    };

    let testFailed = false;

    try {
      await fn({ step: runStep, addMaestroSteps });
      this.runtime.updateTest(testUuid, (result: TestResult) => {
        result.status = Status.PASSED;
        result.stage = Stage.FINISHED;
      });
    } catch (error) {
      testFailed = true;
      this.runtime.updateTest(testUuid, (result: TestResult) => {
        result.status = Status.FAILED;
        result.stage = Stage.FINISHED;
        result.statusDetails = { message: String(error) };
      });
      throw error;
    } finally {
      if (testFailed) {
        this.attachMaestroFailureScreenshot(testUuid, null);
      }
      this.runtime.stopTest(testUuid);
      this.runtime.writeTest(testUuid);
    }
  }

  private importMaestroSteps(
    testUuid: string,
    outputDir: string,
    runnerEnv: Record<string, string>,
  ): void {
    this.lastMaestroSearchRoot = outputDir;

    const commandsPath = resolveMaestroCommandsPath(outputDir);
    if (!commandsPath) {
      return;
    }

    const entries = readMaestroCommands(commandsPath);
    const runOutputDir = path.dirname(commandsPath);
    const steps = buildMaestroAllureSteps(entries, runnerEnv, runOutputDir);
    for (const step of steps) {
      this.writeMaestroStep(testUuid, null, step);
    }
  }

  private writeMaestroStep(
    testUuid: string,
    parentStepUuid: string | null,
    step: MaestroAllureStep,
  ): void {
    const stepUuid = this.runtime.startStep(testUuid, parentStepUuid, {
      name: step.name,
      parameters: step.parameters,
      ...(step.timestamp !== undefined ? { start: step.timestamp } : {}),
    });

    if (!stepUuid) {
      return;
    }

    for (const attachment of this.resolveStepAttachments(step)) {
      this.runtime.writeAttachment(
        testUuid,
        stepUuid,
        attachment.name,
        attachment.path,
        { contentType: attachment.contentType },
      );
    }

    for (const child of step.children) {
      this.writeMaestroStep(testUuid, stepUuid, child);
    }

    this.runtime.updateStep(stepUuid, (result: StepResult) => {
      result.status = step.status;
      result.stage = Stage.FINISHED;
      if (step.statusDetails) {
        result.statusDetails = step.statusDetails;
      }
    });
    this.runtime.stopStep(
      stepUuid,
      step.duration !== undefined ? { duration: step.duration } : undefined,
    );
  }

  private resolveStepAttachments(
    step: MaestroAllureStep,
  ): Array<{ name: string; path: string; contentType: ContentType }> {
    if (step.attachments.length > 0) {
      return step.attachments;
    }

    if (step.status !== Status.FAILED && step.status !== Status.BROKEN) {
      return [];
    }

    const searchRoot = step.runOutputDir ?? this.lastMaestroSearchRoot;
    if (!searchRoot) {
      return [];
    }

    const screenshotPath = findLatestMaestroFailureScreenshot(searchRoot, step.timestamp);
    if (!screenshotPath) {
      return [];
    }

    return [
      {
        name: 'Maestro screenshot',
        path: screenshotPath,
        contentType: ContentType.PNG,
      },
    ];
  }

  private attachMaestroFailureScreenshot(
    testUuid: string,
    parentStepUuid: string | null,
  ): void {
    if (this.testFailureScreenshotAttached || !this.lastMaestroSearchRoot) {
      return;
    }

    const screenshotPath = findLatestMaestroFailureScreenshot(this.lastMaestroSearchRoot);
    if (!screenshotPath) {
      return;
    }

    this.runtime.writeAttachment(
      testUuid,
      parentStepUuid,
      'Maestro screenshot',
      screenshotPath,
      { contentType: ContentType.PNG },
    );
    this.testFailureScreenshotAttached = true;
  }
}
