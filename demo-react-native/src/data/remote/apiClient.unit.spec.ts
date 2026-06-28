import {Platform} from 'react-native';

import {ApiError, apiRequest, parseErrorMessage,} from './apiClient';

const baseUrl = 'http://localhost:8080/v1/';

describe('apiClient', () => {
  beforeEach(() => {
    jest.restoreAllMocks();
    Platform.OS = 'ios';
  });

  describe('API_BASE_URL', () => {
    it('should use android emulator fallback when platform is android', () => {
      let resolvedBaseUrl = '';
      jest.isolateModules(() => {
        const { Platform } = require('react-native');
        Platform.OS = 'android';
        resolvedBaseUrl = require('./apiClient').API_BASE_URL;
      });

      expect(resolvedBaseUrl).toBe('http://10.0.2.2:8080/v1/');
    });
  });

  describe('parseErrorMessage', () => {
    it('should return null when body is empty', () => {
      expect(parseErrorMessage(null)).toBeNull();
      expect(parseErrorMessage('   ')).toBeNull();
    });

    it('should return message from valid json body', () => {
      expect(parseErrorMessage('{"message":"Duplicate title"}')).toBe(
        'Duplicate title',
      );
    });

    it('should return null when json has no message field', () => {
      expect(parseErrorMessage('{"code":"ERR"}')).toBeNull();
    });

    it('should return null when json is invalid', () => {
      expect(parseErrorMessage('not-json')).toBeNull();
    });
  });

  describe('apiRequest', () => {
    it('should normalize base url without trailing slash', async () => {
      global.fetch = jest.fn().mockResolvedValue({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      });

      await apiRequest(baseUrl.replace(/\/$/, ''), 'tasks');

      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/v1/tasks',
        expect.any(Object),
      );
    });

    it('should return parsed json on success', async () => {
      global.fetch = jest.fn().mockResolvedValue({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([{ id: '1' }]),
      });

      const result = await apiRequest<[{ id: string }]>(baseUrl, 'tasks');

      expect(result).toEqual([{ id: '1' }]);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('tasks'),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        }),
      );
    });

    it('should return undefined on 204 response', async () => {
      global.fetch = jest.fn().mockResolvedValue({
        ok: true,
        status: 204,
        text: async () => '',
      });

      await expect(apiRequest<void>(baseUrl, 'tasks/1', { method: 'DELETE' })).resolves.toBeUndefined();
    });

    it('should return undefined when success body is empty', async () => {
      global.fetch = jest.fn().mockResolvedValue({
        ok: true,
        status: 200,
        text: async () => '',
      });

      await expect(apiRequest<void>(baseUrl, 'tasks')).resolves.toBeUndefined();
    });

    it('should throw ApiError with parsed message when request fails', async () => {
      global.fetch = jest.fn().mockResolvedValue({
        ok: false,
        status: 409,
        text: async () => JSON.stringify({ message: 'Title exists' }),
      });

      await expect(apiRequest(baseUrl, 'tasks')).rejects.toEqual(
        new ApiError(409, 'Title exists'),
      );
    });

    it('should throw ApiError with status fallback when error body is empty', async () => {
      global.fetch = jest.fn().mockResolvedValue({
        ok: false,
        status: 500,
        text: async () => '',
      });

      await expect(apiRequest(baseUrl, 'tasks')).rejects.toEqual(
        new ApiError(500, 'Request failed (500)'),
      );
    });

    it('should throw ApiError when response text fails', async () => {
      global.fetch = jest.fn().mockResolvedValue({
        ok: false,
        status: 400,
        text: async () => {
          throw new Error('read failed');
        },
      });

      await expect(apiRequest(baseUrl, 'tasks')).rejects.toEqual(
        new ApiError(400, 'Request failed (400)'),
      );
    });
  });
});
