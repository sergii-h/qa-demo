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
 
const callCounters: Record<string, number> = {};
 
const isMethodResponses = (value: unknown): value is MethodResponses => {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return false;
  }
  return Object.keys(value).some(key => ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'].includes(key.toUpperCase()));
};
 
const mockFetchResponse = (responseMap: ResponseMap, strictMode = false) => {
  Object.keys(callCounters).forEach(key => delete callCounters[key]);

  return vi.mocked(globalThis.fetch as ReturnType<typeof vi.fn>).mockImplementation((url: string, requestPayload: unknown) => {
    const method = (requestPayload as { method?: string } | undefined)?.method || 'GET';

    const matchingKey = Object.keys(responseMap).find(key => url.includes(key));

    if (matchingKey !== undefined) {
      const config = responseMap[matchingKey];
      let responses: ResponseMapEntry | ResponseMapEntry[];

      // New syntax: { GET: {...}, POST: {...} }
      if (isMethodResponses(config)) {
        const methodConfig = config[method] || config[method.toUpperCase()] || config[method.toLowerCase()];
        if (!methodConfig) {
          return Promise.resolve({
            ok: false,
            status: 404,
            statusText: 'Not Found',
            headers: {
              get: () => null,
            },
            json: async () => ({}),
          } as unknown as Response);
        }
        responses = methodConfig;
      }
      // Simple syntax: { body: {...} } or array
      else {
        responses = config as ResponseMapEntry | ResponseMapEntry[];
      }

      const isSequentialResponse = Array.isArray(responses);
      let entry: ResponseMapEntry;

      if (isSequentialResponse) {
        const counterKey = `${matchingKey}:${method}`;
        callCounters[counterKey] = (callCounters[counterKey] || 0) + 1;
        const callIndex = callCounters[counterKey] - 1;
        const responseIndex = Math.min(callIndex, (responses as ResponseMapEntry[]).length - 1);
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

      // Simulate browser behavior: automatically set cookies from Set-Cookie headers
      if (entry.headers && entry.headers['set-cookie']) {
        const cookieValue = entry.headers['set-cookie'];
        // Extract cookie name=value from Set-Cookie header (simplified parsing)
        // Format: "name=value; Path=/; HttpOnly" -> extract "name=value"
        const cookiePair = cookieValue.split(';')[0];

        // Update document.cookie (simulating browser behavior)
        if (typeof document !== 'undefined') {
          try {
            // Delete existing property first to avoid "Cannot redefine property" errors
            delete (document as unknown as Record<string, unknown>).cookie;
          } catch {
            // Ignore errors if property can't be deleted
          }

          const existingCookies = '';
          const newCookie = existingCookies ? `${existingCookies}; ${cookiePair}` : cookiePair;
          Object.defineProperty(document, 'cookie', {
            writable: true,
            configurable: true, // Allow property to be redefined in future tests
            value: newCookie,
          });
        }
      }

      const status = entry.status ?? 200;
      const ok = entry.ok ?? (status >= 200 && status < 300);

      return Promise.resolve({
        ok,
        status,
        statusText: entry.statusText ?? 'OK',
        headers: {
          get: (name: string) => {
            if (entry.headers && entry.headers[name.toLowerCase()]) {
              return entry.headers[name.toLowerCase()];
            }
            if (name === 'content-type') return 'application/json';
            if (name === 'content-length') return '100';
            return null;
          },
        },
        json: async () => entry.body,
      } as Response);
    }

    if (strictMode) {
      console.error(`[mockFetchResponse] Unexpected API call to: ${url} [${method}]`);
      return Promise.reject({
        ok: false,
        status: 404,
        statusText: 'Not Found',
        json: async () => ({}),
      });
    }

    // Return a 404 response when no match is found (non-strict mode)
    return Promise.resolve({
      ok: false,
      status: 404,
      statusText: 'Not Found',
      headers: {
        get: () => null,
      },
      json: async () => ({}),
    } as unknown as Response);
  });
};

export { mockFetchResponse, type MethodResponses, type ResponseMap, type ResponseMapEntry };
