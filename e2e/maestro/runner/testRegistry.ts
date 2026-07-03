import fs from 'node:fs';
import path from 'node:path';
import { pathToFileURL } from 'node:url';

import type { MaestroTestCase, TestSuite } from '@/runner/types';

export type { MaestroTestCase, TestSuite } from '@/runner/types';

function discoverTestFiles(testsDir: string): string[] {
  const files: string[] = [];

  function walk(directory: string): void {
    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
      const entryPath = path.join(directory, entry.name);
      if (entry.isDirectory()) {
        walk(entryPath);
        continue;
      }

      if (entry.name.endsWith('.test.ts')) {
        files.push(entryPath);
      }
    }
  }

  walk(testsDir);
  return files.sort();
}

let cachedTestCases: MaestroTestCase[] | undefined;

export async function loadTestCases(maestroRoot: string): Promise<MaestroTestCase[]> {
  if (cachedTestCases) {
    return cachedTestCases;
  }

  const testsDir = path.join(maestroRoot, 'tests');
  const testFiles = discoverTestFiles(testsDir);
  const testCases: MaestroTestCase[] = [];

  for (const filePath of testFiles) {
    const module = await import(pathToFileURL(filePath).href);
    const testCase = module.default as MaestroTestCase | undefined;

    if (!testCase?.name || !testCase?.flow) {
      throw new Error(`Test file must default-export a MaestroTestCase: ${filePath}`);
    }

    testCases.push(testCase);
  }

  cachedTestCases = testCases;
  return testCases;
}

export async function testsForSuite(
  maestroRoot: string,
  suite: TestSuite,
): Promise<MaestroTestCase[]> {
  const testCases = await loadTestCases(maestroRoot);
  return testCases.filter((testCase) => testCase.suite === suite);
}
