import { testConfig } from '@/test.config';

type ScenarioMapping = {
  scenarioName: string;
  request: Record<string, string>;
  response: Record<string, unknown>;
};

const WIREMOCK_RETRY_ATTEMPTS = 5;
const WIREMOCK_RETRY_DELAY_MS = 200;

export class WireMockClient {
  private readonly baseUrl = testConfig.wiremock.url.replace(/\/$/, '');
  private readonly plannedScenarioStates = new Map<string, string>();

  async ensureReady(): Promise<this> {
    await this.requestAdmin('GET', '/__admin/health');
    return this;
  }

  async clearMocks(): Promise<this> {
    await this.requestAdmin('DELETE', '/__admin/mappings');
    return this;
  }

  async resetScenarios(): Promise<this> {
    await this.requestAdmin('POST', '/__admin/scenarios/reset', '{}');
    this.plannedScenarioStates.clear();
    return this;
  }

  async reset(): Promise<this> {
    await this.clearMocks();
    await this.resetScenarios();
    return this;
  }

  async addScenarioMapping(mapping: ScenarioMapping): Promise<this> {
    const requiredState =
      this.plannedScenarioStates.get(mapping.scenarioName) ?? 'Started';
    const newState = nextScenarioState(requiredState);
    this.plannedScenarioStates.set(mapping.scenarioName, newState);

    await this.requestAdmin(
      'POST',
      '/__admin/mappings',
      JSON.stringify({
        scenarioName: mapping.scenarioName,
        requiredScenarioState: requiredState,
        newScenarioState: newState,
        request: mapping.request,
        response: mapping.response,
      }),
    );

    return this;
  }

  private async requestAdmin(
    method: 'GET' | 'POST' | 'DELETE',
    path: string,
    body?: string,
  ): Promise<void> {
    const url = `${this.baseUrl}${path}`;
    let lastError: unknown;

    for (let attempt = 1; attempt <= WIREMOCK_RETRY_ATTEMPTS; attempt++) {
      try {
        const response = await fetch(url, {
          method,
          headers: body ? { 'Content-Type': 'application/json' } : undefined,
          body,
        });

        if (response.ok) {
          return;
        }

        lastError = new Error(`HTTP ${response.status}`);
      } catch (error) {
        lastError = error;
      }

      if (attempt < WIREMOCK_RETRY_ATTEMPTS) {
        await sleep(WIREMOCK_RETRY_DELAY_MS * attempt);
      }
    }

    const causeMessage =
      lastError instanceof Error ? lastError.message : String(lastError);

    throw new Error(
      `WireMock ${method} ${path} failed after ${WIREMOCK_RETRY_ATTEMPTS} attempts at ${this.baseUrl}. ` +
        `Is WireMock running? Start it with: docker compose -f docker/docker-compose/run-application.yml up -d qa-demo-wiremock. ` +
        `Cause: ${causeMessage}`,
      { cause: lastError },
    );
  }
}

function nextScenarioState(currentState: string): string {
  if (currentState === 'Started') {
    return 'step-1';
  }
  const step = Number.parseInt(currentState.replace('step-', ''), 10);
  return `step-${step + 1}`;
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
