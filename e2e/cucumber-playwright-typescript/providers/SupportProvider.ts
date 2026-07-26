import { APIRequestContext, Page } from '@playwright/test';
import { ApiClient } from '@/support/api/ApiClient';
import { WireMockClient } from '@/support/mocks/WireMockClient';
import { ApiRouteMock } from '@/support/mocks/ApiRouteMock';

export class SupportProvider {
  readonly api: ApiClient;
  readonly wiremock: WireMockClient;
  readonly mock: { api: ApiRouteMock };

  constructor(page: Page, request: APIRequestContext) {
    this.api = new ApiClient(request);
    this.wiremock = new WireMockClient(request);
    this.mock = { api: new ApiRouteMock(page) };
  }
}
