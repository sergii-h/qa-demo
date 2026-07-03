import { AllureClient } from '@/support/allure/AllureClient';
import { ApiRouteMock } from '@/support/mocks/ApiRouteMock';
import { WireMockClient } from '@/support/mocks/WireMockClient';
import type { TestSuite } from '@/runner/types';

export class SupportProvider {
  readonly wiremock: WireMockClient;
  readonly mock: { api: ApiRouteMock };
  readonly allure: AllureClient;

  constructor(resultsDir: string, suite: TestSuite) {
    this.wiremock = new WireMockClient();
    this.mock = { api: new ApiRouteMock(this.wiremock) };
    this.allure = new AllureClient(resultsDir, suite);
  }

  async reset(): Promise<void> {
    await this.wiremock.reset();
    await this.mock.api.getTasks();
  }
}
