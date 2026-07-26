import { APIRequestContext } from '@playwright/test';
import { testConfig } from '@/test.config';

export class WireMockClient {
  private readonly baseUrl = testConfig.wiremock.url;

  constructor(private readonly request: APIRequestContext) {}

  async clearMocks(): Promise<this> {
    await this.request.delete(`${this.baseUrl}/__admin/mappings`);
    return this;
  }

  async stubTaskValidation(isValid: boolean): Promise<this> {
    await this.request.post(`${this.baseUrl}/__admin/mappings`, {
      data: {
        request: {
          method: 'GET',
          urlPattern: '/api/tasks/.*/validation',
        },
        response: {
          status: 200,
          jsonBody: { valid: isValid },
        },
      },
    });
    return this;
  }
}
