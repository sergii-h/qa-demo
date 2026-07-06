import path from 'node:path';
import { fileURLToPath } from 'node:url';

import type { MaestroTestCase } from '@/runner/types';

export function maestroTestCase(
  moduleUrl: string,
  testCase: Omit<MaestroTestCase, 'flow'>,
): MaestroTestCase {
  const testFilePath = fileURLToPath(moduleUrl);
  const testDirectory = path.dirname(testFilePath);
  const flowBaseName = path.basename(testFilePath, '.test.ts');
  const maestroRoot = path.resolve(testDirectory, '../..');
  const flow = path.relative(maestroRoot, path.join(testDirectory, `${flowBaseName}.yaml`));

  return {
    ...testCase,
    flow,
  };
}
