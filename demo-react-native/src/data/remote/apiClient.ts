import Constants from 'expo-constants';
import {Platform} from 'react-native';

import {ErrorResponse} from '../models/task';

const fallbackApiBaseUrl =
  Platform.OS === 'android'
    ? 'http://10.0.2.2:8080/v1/'
    : 'http://localhost:8080/v1/';

export const API_BASE_URL: string =
  (Constants.expoConfig?.extra?.apiBaseUrl as string | undefined) ||
  fallbackApiBaseUrl;

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export function parseErrorMessage(body: string | null): string | null {
  if (!body?.trim()) {
    return null;
  }
  try {
    const parsed = JSON.parse(body) as ErrorResponse;
    return parsed.message ?? null;
  } catch {
    return null;
  }
}

async function parseResponseBody(response: Response): Promise<string | null> {
  try {
    return await response.text();
  } catch {
    return null;
  }
}

export async function apiRequest<T>(
  baseUrl: string,
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const normalizedBase = baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`;
  const response = await fetch(`${normalizedBase}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    const body = await parseResponseBody(response);
    const message = parseErrorMessage(body) ?? `Request failed (${response.status})`;
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}
