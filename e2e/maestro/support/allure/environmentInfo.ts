import { testConfig } from '@/test.config';
import type { TestSuite } from '@/runner/types';
import type { EnvironmentInfo } from 'allure-js-commons/sdk';

export function buildMaestroEnvironmentInfo(suite: TestSuite): EnvironmentInfo {
  const environment =
    suite === 'uat' ? testConfig.api.url : testConfig.wiremock.url;

  return {
    Framework: 'Maestro',
    suite,
    environment,
    app_id: testConfig.maestro.appId,
  };
}
