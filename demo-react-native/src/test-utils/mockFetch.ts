type ResponseMapEntry = {
  body: unknown;
  ok?: boolean;
  status?: number;
  statusText?: string;
  headers?: Record<string, string>;
  reject?: boolean | string | Error;
};

type MethodResponses = {
  [method: string]: ResponseMapEntry | ResponseMapEntry[];
};

type ResponseMap = Record<string, ResponseMapEntry | ResponseMapEntry[] | MethodResponses>;

const isMethodResponses = (value: unknown): value is MethodResponses => {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return false;
  }
  return Object.keys(value).some((key) =>
    ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'].includes(
      key.toUpperCase(),
    ),
  );
};

export function mockFetchResponse(responseMap: ResponseMap) {
  const callCounters: Record<string, number> = {};

  return jest
    .mocked(global.fetch as jest.Mock)
    .mockImplementation((url: string, requestPayload?: RequestInit) => {
      const method = requestPayload?.method || 'GET';
      const matchingKey = Object.keys(responseMap)
        .filter((key) => url.includes(key))
        .sort((a, b) => b.length - a.length)[0];

      if (matchingKey === undefined) {
        return Promise.resolve({
          ok: false,
          status: 404,
          statusText: 'Not Found',
          text: async () => '',
        } as Response);
      }

      const config = responseMap[matchingKey];
      let responses: ResponseMapEntry | ResponseMapEntry[];

      if (isMethodResponses(config)) {
        const methodConfig =
          config[method] || config[method.toUpperCase()] || config[method.toLowerCase()];
        if (!methodConfig) {
          return Promise.resolve({
            ok: false,
            status: 404,
            statusText: 'Not Found',
            text: async () => '',
          } as Response);
        }
        responses = methodConfig;
      } else {
        responses = config as ResponseMapEntry | ResponseMapEntry[];
      }

      const isSequentialResponse = Array.isArray(responses);
      let entry: ResponseMapEntry;

      if (isSequentialResponse) {
        const counterKey = `${matchingKey}:${method}`;
        callCounters[counterKey] = (callCounters[counterKey] || 0) + 1;
        const callIndex = callCounters[counterKey] - 1;
        const responseIndex = Math.min(
          callIndex,
          (responses as ResponseMapEntry[]).length - 1,
        );
        entry = (responses as ResponseMapEntry[])[responseIndex] as ResponseMapEntry;
      } else {
        entry = responses as ResponseMapEntry;
      }

      if (entry.reject) {
        if (entry.reject === true) {
          return Promise.reject(new Error('Network error'));
        }
        if (entry.reject instanceof Error) {
          return Promise.reject(entry.reject);
        }
        return Promise.reject(new Error(entry.reject));
      }

      const status = entry.status ?? 200;
      const ok = entry.ok ?? (status >= 200 && status < 300);
      const bodyText =
        entry.body === undefined || entry.body === null
          ? ''
          : JSON.stringify(entry.body);

      return Promise.resolve({
        ok,
        status,
        statusText: entry.statusText ?? 'OK',
        headers: {
          get: (name: string) => {
            if (entry.headers?.[name.toLowerCase()]) {
              return entry.headers[name.toLowerCase()];
            }
            if (name.toLowerCase() === 'content-type') {
              return 'application/json';
            }
            return null;
          },
        },
        text: async () => bodyText,
      } as Response);
    });
}

export type { MethodResponses, ResponseMap, ResponseMapEntry };
