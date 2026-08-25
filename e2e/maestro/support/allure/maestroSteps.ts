import fs from 'node:fs';
import path from 'node:path';

import { ContentType, Status, type Parameter } from 'allure-js-commons';

export type MaestroCommandEntry = {
  command: Record<string, unknown>;
  metadata: {
    status: string;
    timestamp: number;
    duration: number;
    sequenceNumber: number;
    evaluatedCommand?: Record<string, unknown>;
    error?: {
      message?: string;
    };
  };
};

export type MaestroAllureStep = {
  name: string;
  parameters: Parameter[];
  status: Status;
  duration?: number;
  statusDetails?: { message?: string };
  children: MaestroAllureStep[];
  attachments: Array<{ name: string; path: string; contentType: ContentType }>;
  timestamp?: number;
  runOutputDir?: string;
};

const INTERNAL_ENV_PREFIXES = ['MAESTRO_'];

export function resolveMaestroCommandsPath(outputDir: string): string | undefined {
  const matches = findMaestroCommandsFiles(outputDir);
  if (matches.length === 0) {
    return undefined;
  }

  return matches.sort(
    (left, right) => fs.statSync(right).mtimeMs - fs.statSync(left).mtimeMs,
  )[0];
}

function findMaestroCommandsFiles(directory: string): string[] {
  if (!fs.existsSync(directory)) {
    return [];
  }

  const files: string[] = [];
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const entryPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      files.push(...findMaestroCommandsFiles(entryPath));
      continue;
    }

    if (isMaestroCommandsFileName(entry.name)) {
      files.push(entryPath);
    }
  }

  return files;
}

function isMaestroCommandsFileName(fileName: string): boolean {
  if (!fileName.endsWith('.json')) {
    return false;
  }

  return fileName === 'commands.json' || fileName.startsWith('commands-');
}

export function readMaestroCommands(commandsPath: string): MaestroCommandEntry[] {
  const parsed = JSON.parse(fs.readFileSync(commandsPath, 'utf8')) as unknown;
  const rawEntries = extractRawCommandEntries(parsed);

  return rawEntries
    .map((raw, index) => normalizeMaestroCommand(raw, index))
    .sort((left, right) => left.metadata.sequenceNumber - right.metadata.sequenceNumber);
}

function extractRawCommandEntries(parsed: unknown): unknown[] {
  if (Array.isArray(parsed)) {
    return parsed;
  }

  if (parsed && typeof parsed === 'object') {
    const wrapped = parsed as { commands?: unknown };
    if (Array.isArray(wrapped.commands)) {
      return wrapped.commands;
    }
  }

  return [];
}

function normalizeMaestroCommand(raw: unknown, index: number): MaestroCommandEntry {
  const record = (raw && typeof raw === 'object' ? raw : {}) as Record<string, unknown>;
  const nestedMetadata =
    record.metadata && typeof record.metadata === 'object'
      ? (record.metadata as Record<string, unknown>)
      : {};
  const command =
    record.command && typeof record.command === 'object'
      ? (record.command as Record<string, unknown>)
      : {};
  const error =
    (nestedMetadata.error as { message?: string } | undefined) ??
    (record.error as { message?: string } | undefined);

  return {
    command,
    metadata: {
      status: String(nestedMetadata.status ?? record.status ?? 'COMPLETED'),
      timestamp: Number(
        nestedMetadata.timestamp ?? record.timestamp ?? record.startedAt ?? 0,
      ),
      duration: Number(
        nestedMetadata.duration ?? record.duration ?? record.durationMs ?? 0,
      ),
      sequenceNumber: Number(
        nestedMetadata.sequenceNumber ??
          record.sequenceNumber ??
          record.sequence ??
          index,
      ),
      evaluatedCommand:
        (nestedMetadata.evaluatedCommand as Record<string, unknown> | undefined) ??
        (record.evaluatedCommand as Record<string, unknown> | undefined),
      error,
    },
  };
}

export function buildMaestroAllureSteps(
  entries: MaestroCommandEntry[],
  runnerEnv: Record<string, string>,
  outputDir: string,
): MaestroAllureStep[] {
  const env = filterTestDataEnv(runnerEnv);
  const strictSteps = collectTopLevelSteps(entries, { ...env }, outputDir, {
    allowFilenameOnly: false,
  });

  if (strictSteps.length > 0) {
    return strictSteps;
  }

  const fileNameSteps = collectTopLevelSteps(entries, { ...env }, outputDir, {
    allowFilenameOnly: true,
  });

  if (fileNameSteps.length > 0) {
    return fileNameSteps;
  }

  return collectTopLevelSteps(entries, { ...env }, outputDir, { includeLeaves: true });
}

function collectTopLevelSteps(
  entries: MaestroCommandEntry[],
  env: Record<string, string>,
  outputDir: string,
  options: { allowFilenameOnly?: boolean; includeLeaves?: boolean },
): MaestroAllureStep[] {
  const steps: MaestroAllureStep[] = [];

  for (const entry of entries) {
    const evaluatedCommand = entry.metadata.evaluatedCommand ?? entry.command;
    const commandType = Object.keys(evaluatedCommand)[0] ?? '';

    if (
      !shouldIncludeTopLevelCommand(commandType, evaluatedCommand, options) &&
      !shouldIncludeLeafFallback(commandType, options)
    ) {
      continue;
    }

    mergeEnvFromCommand(evaluatedCommand, env);

    if (isDefineVariablesType(commandType)) {
      continue;
    }

    const built = buildStepFromEntry(
      entry,
      mapMaestroStatus(entry.metadata.status),
      env,
      outputDir,
      entries,
      { includeParameters: true },
    );

    if (built) {
      steps.push(built);
    }
  }

  return steps;
}

function buildStepFromEntry(
  entry: MaestroCommandEntry,
  status: Status,
  env: Record<string, string>,
  outputDir: string,
  allEntries: MaestroCommandEntry[],
  options: { includeParameters: boolean },
  parentStatus?: Status,
): MaestroAllureStep | null {
  const evaluatedCommand = entry.metadata.evaluatedCommand ?? entry.command;
  const commandType = Object.keys(evaluatedCommand)[0] ?? '';

  mergeEnvFromCommand(evaluatedCommand, env);

  if (isDefineVariablesType(commandType) || isApplyConfigurationType(commandType)) {
    return null;
  }

  const effectiveStatus =
    parentStatus && !isFailureStatus(status) ? parentStatus : status;

  return buildStepFromEvaluatedCommand(
    evaluatedCommand,
    effectiveStatus,
    entry.metadata.duration,
    entry.metadata.error?.message,
    env,
    outputDir,
    entry.metadata.timestamp,
    options,
    collectChildEntries(entry, allEntries),
    allEntries,
  );
}

function collectChildEntries(
  parent: MaestroCommandEntry,
  entries: MaestroCommandEntry[],
): MaestroCommandEntry[] {
  const windowStart = parent.metadata.timestamp;
  const windowEnd = windowStart + parent.metadata.duration;

  const candidates = entries.filter((entry) => {
    if (entry.metadata.sequenceNumber === parent.metadata.sequenceNumber) {
      return false;
    }

    const entryEnd = entry.metadata.timestamp + entry.metadata.duration;
    return entry.metadata.timestamp >= windowStart && entryEnd <= windowEnd;
  });

  const nestedRunFlows = candidates.filter(isRunFlowEntry);

  return candidates.filter(
    (entry) =>
      isRunFlowEntry(entry) ||
      !isWithinNestedRunFlow(entry, nestedRunFlows),
  );
}

function isRunFlowEntry(entry: MaestroCommandEntry): boolean {
  const evaluatedCommand = entry.metadata.evaluatedCommand ?? entry.command;
  return isRunFlowType(Object.keys(evaluatedCommand)[0] ?? '');
}

function isWithinNestedRunFlow(
  entry: MaestroCommandEntry,
  nestedRunFlows: MaestroCommandEntry[],
): boolean {
  return nestedRunFlows.some((runFlow) => {
    if (runFlow.metadata.sequenceNumber === entry.metadata.sequenceNumber) {
      return false;
    }

    const runFlowEnd = runFlow.metadata.timestamp + runFlow.metadata.duration;
    const entryEnd = entry.metadata.timestamp + entry.metadata.duration;
    return (
      entry.metadata.timestamp >= runFlow.metadata.timestamp &&
      entryEnd <= runFlowEnd
    );
  });
}

function buildStepFromEvaluatedCommand(
  evaluatedCommand: Record<string, unknown>,
  status: Status,
  duration: number | undefined,
  errorMessage: string | undefined,
  env: Record<string, string>,
  outputDir: string,
  timestamp: number,
  options: { includeParameters: boolean },
  childEntries: MaestroCommandEntry[] = [],
  allEntries: MaestroCommandEntry[] = [],
): MaestroAllureStep | null {
  const [commandType, payload] = Object.entries(evaluatedCommand)[0] ?? [];

  if (!commandType || !payload || typeof payload !== 'object') {
    return null;
  }

  if (isApplyConfigurationType(commandType)) {
    return null;
  }

  if (isDefineVariablesType(commandType)) {
    mergeEnvFromCommand(evaluatedCommand, env);
    return null;
  }

  if (isRunFlowType(commandType)) {
    const runFlow = payload as RunFlowCommand;
    mergeEnvFromCommand({ defineVariablesCommand: { env: runFlow.config?.env } }, env);

    const name =
      runFlow.label ??
      formatSourceDescription(runFlowSource(runFlow)) ??
      formatRunFlowCondition(runFlow.condition, env);
    const flowEnv = filterTestDataEnv(runFlow.config?.env ?? {});
    const mergedEnv = { ...env, ...flowEnv };
    const stepEnv = options.includeParameters ? mergedEnv : {};

    return {
      name,
      parameters: toParameters(stepEnv),
      status,
      duration,
      statusDetails: errorMessage ? { message: errorMessage } : undefined,
      children: buildChildStepsFromEntries(
        childEntries,
        status,
        mergedEnv,
        outputDir,
        allEntries,
      ),
      attachments: findFailureAttachments(outputDir, timestamp, status),
      timestamp,
      runOutputDir: outputDir,
    };
  }

  if (commandType === 'repeatCommand') {
    const repeat = payload as RepeatCommand;
    return {
      name: formatRepeatCommand(repeat, env),
      parameters: toParameters(options.includeParameters ? env : {}),
      status,
      duration,
      statusDetails: errorMessage ? { message: errorMessage } : undefined,
      children: buildChildStepsFromEntries(
        childEntries,
        status,
        env,
        outputDir,
        allEntries,
      ),
      attachments: findFailureAttachments(outputDir, timestamp, status),
      timestamp,
      runOutputDir: outputDir,
    };
  }

  const name = describeLeafCommand(commandType, payload as Record<string, unknown>, env);
  if (!name) {
    return null;
  }

  return {
    name,
    parameters: toParameters(options.includeParameters ? env : {}),
    status,
    duration,
    statusDetails: errorMessage ? { message: errorMessage } : undefined,
    children: [],
    attachments: findFailureAttachments(outputDir, timestamp, status),
    timestamp,
    runOutputDir: outputDir,
  };
}

function buildChildStepsFromEntries(
  childEntries: MaestroCommandEntry[],
  parentStatus: Status,
  env: Record<string, string>,
  outputDir: string,
  allEntries: MaestroCommandEntry[],
): MaestroAllureStep[] {
  const steps: MaestroAllureStep[] = [];

  for (const childEntry of childEntries) {
    const built = buildStepFromEntry(
      childEntry,
      mapMaestroStatus(childEntry.metadata.status),
      env,
      outputDir,
      allEntries,
      { includeParameters: false },
      parentStatus,
    );

    if (built) {
      steps.push(built);
    }
  }

  return steps;
}

function describeLeafCommand(
  commandType: string,
  payload: Record<string, unknown>,
  env: Record<string, string>,
): string | null {
  switch (commandType) {
    case 'launchAppCommand': {
      const clearState = payload.clearState === true;
      return clearState ? 'Launch app (clear state)' : 'Launch app';
    }
    case 'tapOnElement':
      return `Tap on ${formatSelector(payload.selector as Selector | undefined, env)}`;
    case 'inputTextCommand':
      return `Input text: ${resolveEnvPlaceholders(String(payload.text ?? ''), env)}`;
    case 'eraseTextCommand':
      return 'Clear text field';
    case 'hideKeyboardCommand':
      return 'Hide keyboard';
    case 'swipeCommand':
      return `Swipe ${payload.startRelative ?? payload.startPoint ?? ''} → ${payload.endRelative ?? payload.endPoint ?? ''}`.trim();
    case 'assertConditionCommand':
      return formatAssertion(payload.condition as AssertionCondition | undefined, env);
    case 'runScriptCommand':
      return 'Run script';
    default:
      return commandType.replace(/Command$/, '');
  }
}

function formatAssertion(
  condition: AssertionCondition | undefined,
  env: Record<string, string> = {},
): string | null {
  if (!condition) {
    return 'Assert condition';
  }

  if (condition.visible) {
    return `Assert visible: ${formatSelector(condition.visible, env)}`;
  }

  if (condition.notVisible) {
    return `Assert not visible: ${formatSelector(condition.notVisible, env)}`;
  }

  return 'Assert condition';
}

function formatSelector(selector: Selector | undefined, env: Record<string, string> = {}): string {
  if (!selector) {
    return 'element';
  }

  if (selector.text) {
    return `text "${resolveEnvPlaceholders(selector.text, env)}"`;
  }

  if (selector.textRegex) {
    return `text /${resolveEnvPlaceholders(selector.textRegex, env)}/`;
  }

  if (selector.idRegex) {
    return `id /${resolveEnvPlaceholders(selector.idRegex, env)}/`;
  }

  if (selector.id) {
    return `id "${resolveEnvPlaceholders(selector.id, env)}"`;
  }

  const below = selector.below as Selector | undefined;
  if (below?.text) {
    return `element below text "${resolveEnvPlaceholders(below.text, env)}"`;
  }

  return 'element';
}

function resolveEnvPlaceholders(value: string, env: Record<string, string>): string {
  const placeholderPattern = new RegExp(String.raw`\$\{([^}]+)\}`, 'g');
  return value.replace(placeholderPattern, (_, key: string) => env[key] ?? `\${${key}}`);
}

function formatSourceDescription(sourceDescription: string | undefined): string | undefined {
  if (!sourceDescription) {
    return undefined;
  }

  const baseName = path.basename(sourceDescription, path.extname(sourceDescription));
  return baseName
    .split(/[-_]/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function formatRunFlowCondition(
  condition: AssertionCondition | undefined,
  env: Record<string, string> = {},
): string {
  if (!condition) {
    return 'Run flow';
  }

  if (condition.notVisible) {
    return `When not visible: ${formatSelector(condition.notVisible, env)}`;
  }

  if (condition.visible) {
    return `When visible: ${formatSelector(condition.visible, env)}`;
  }

  return 'Run flow';
}

function formatRepeatCommand(repeat: RepeatCommand, env: Record<string, string> = {}): string {
  const times = repeat.times ?? '?';
  if (repeat.condition?.notVisible) {
    return `Repeat ${times} while not visible: ${formatSelector(repeat.condition.notVisible, env)}`;
  }

  if (repeat.condition?.visible) {
    return `Repeat ${times} while visible: ${formatSelector(repeat.condition.visible, env)}`;
  }

  return `Repeat ${times} times`;
}

function mergeEnvFromCommand(
  command: Record<string, unknown>,
  env: Record<string, string>,
): void {
  const defineVariables = (command.defineVariablesCommand ?? command.defineVariables) as
    | { env?: Record<string, string> }
    | undefined;

  if (!defineVariables?.env) {
    return;
  }

  for (const [key, value] of Object.entries(defineVariables.env)) {
    if (isTestDataEnvKey(key) && value !== 'undefined' && !value.includes('${')) {
      env[key] = value;
    }
  }
}

function shouldIncludeTopLevelCommand(
  commandType: string,
  evaluatedCommand: Record<string, unknown>,
  options: { allowFilenameOnly?: boolean; includeLeaves?: boolean } = {},
): boolean {
  if (isDefineVariablesType(commandType)) {
    return true;
  }

  if (!isRunFlowType(commandType)) {
    return false;
  }

  const runFlow = Object.values(evaluatedCommand)[0] as RunFlowCommand;
  const source = runFlowSource(runFlow);

  if (!source && runFlow.condition) {
    return false;
  }

  if (isPageSource(source)) {
    return false;
  }

  if (isStepOrValidatorSource(source)) {
    return true;
  }

  return (
    options.allowFilenameOnly === true &&
    (Boolean(source) || Boolean(runFlow.label) || (runFlow.commands?.length ?? 0) > 0)
  );
}

function shouldIncludeLeafFallback(
  commandType: string,
  options: { includeLeaves?: boolean },
): boolean {
  if (options.includeLeaves !== true) {
    return false;
  }

  return !isDefineVariablesType(commandType) && !isApplyConfigurationType(commandType);
}

function isRunFlowType(commandType: string): boolean {
  return commandType === 'runFlowCommand' || commandType === 'runFlow';
}

function isDefineVariablesType(commandType: string): boolean {
  return commandType === 'defineVariablesCommand' || commandType === 'defineVariables';
}

function isApplyConfigurationType(commandType: string): boolean {
  return commandType === 'applyConfigurationCommand' || commandType === 'applyConfiguration';
}

function runFlowSource(runFlow: RunFlowCommand): string {
  return runFlow.sourceDescription ?? runFlow.file ?? runFlow.source ?? '';
}

function isPageSource(source: string): boolean {
  return /(?:^|[/\\])pages[/\\]/.test(source) || source.startsWith('../pages/');
}

function isStepOrValidatorSource(source: string): boolean {
  return (
    /(?:^|[/\\])steps[/\\]/.test(source) ||
    /(?:^|[/\\])validators[/\\]/.test(source) ||
    source.includes('/interactions/') ||
    source.startsWith('../../interactions/')
  );
}

function filterTestDataEnv(env: Record<string, string>): Record<string, string> {
  return Object.fromEntries(
    Object.entries(env).filter(
      ([key, value]) =>
        isTestDataEnvKey(key) &&
        value !== 'undefined' &&
        !value.includes('${'),
    ),
  );
}

function isTestDataEnvKey(key: string): boolean {
  if (INTERNAL_ENV_PREFIXES.some((prefix) => key.startsWith(prefix))) {
    return false;
  }

  return (
    key.startsWith('TASK_') ||
    key.startsWith('UPDATED_') ||
    key.startsWith('SCROLL_')
  );
}

function toParameters(env: Record<string, string>): Parameter[] {
  return Object.entries(env)
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([name, value]) => ({ name, value }));
}

function mapMaestroStatus(status: string): Status {
  switch (status) {
    case 'COMPLETED':
    case 'SUCCESS':
    case 'PASSED':
      return Status.PASSED;
    case 'FAILED':
      return Status.FAILED;
    case 'SKIPPED':
      return Status.SKIPPED;
    case 'ERROR':
    case 'WARNED':
      return Status.BROKEN;
    default:
      return Status.BROKEN;
  }
}

function isFailureStatus(status: Status): boolean {
  return status === Status.FAILED || status === Status.BROKEN;
}

export function findLatestMaestroFailureScreenshot(
  searchRoot: string,
  nearTimestamp?: number,
): string | undefined {
  const screenshots = findMaestroFailureScreenshots(searchRoot);
  if (screenshots.length === 0) {
    return undefined;
  }

  if (!nearTimestamp) {
    return screenshots[0]?.path;
  }

  return [...screenshots].sort(
    (left, right) =>
      Math.abs(left.timestamp - nearTimestamp) - Math.abs(right.timestamp - nearTimestamp),
  )[0]?.path;
}

export function findMaestroFailureScreenshots(
  searchRoot: string,
): Array<{ path: string; timestamp: number }> {
  if (!fs.existsSync(searchRoot)) {
    return [];
  }

  const screenshots: Array<{ path: string; timestamp: number }> = [];
  collectFailureScreenshots(searchRoot, screenshots);
  return screenshots.sort((left, right) => right.timestamp - left.timestamp);
}

function collectFailureScreenshots(
  directory: string,
  screenshots: Array<{ path: string; timestamp: number }>,
): void {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const entryPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      collectFailureScreenshots(entryPath, screenshots);
      continue;
    }

    if (!entry.name.startsWith('screenshot-') || !entry.name.endsWith('.png')) {
      continue;
    }

    const match = entry.name.match(/screenshot-(?:❌-)?(\d+)-/);
    screenshots.push({
      path: entryPath,
      timestamp: match ? Number.parseInt(match[1] ?? '', 10) : 0,
    });
  }
}

function findFailureAttachments(
  outputDir: string,
  timestamp: number,
  status: Status,
): Array<{ name: string; path: string; contentType: ContentType }> {
  if (!isFailureStatus(status) || !outputDir) {
    return [];
  }

  const screenshotPath = findLatestMaestroFailureScreenshot(outputDir, timestamp || undefined);
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

type Selector = {
  text?: string;
  textRegex?: string;
  id?: string;
  idRegex?: string;
  below?: Selector;
};

type AssertionCondition = {
  visible?: Selector;
  notVisible?: Selector;
};

type RunFlowCommand = {
  label?: string;
  sourceDescription?: string;
  file?: string;
  source?: string;
  condition?: AssertionCondition;
  config?: {
    env?: Record<string, string>;
  };
  commands?: Array<Record<string, unknown>>;
};

type RepeatCommand = {
  times?: string | number;
  condition?: AssertionCondition;
  commands?: Array<Record<string, unknown>>;
};
